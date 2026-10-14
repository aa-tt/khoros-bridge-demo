import json
import os
import urllib.parse
import urllib.request
from datetime import datetime, timezone

import boto3

secrets = boto3.client("secretsmanager")
dynamodb = boto3.resource("dynamodb")
s3 = boto3.client("s3")


def _request(url, *, data=None, headers=None):
    request = urllib.request.Request(url, data=data, headers=headers or {})
    with urllib.request.urlopen(request, timeout=20) as response:
        return json.loads(response.read().decode("utf-8"))


def _salesforce_records(secret):
    login_url = secret.get("login_url", "https://login.salesforce.com").rstrip("/")
    token_body = urllib.parse.urlencode({
        "grant_type": "refresh_token",
        "client_id": secret["client_id"],
        "client_secret": secret["client_secret"],
        "refresh_token": secret["refresh_token"],
    }).encode("utf-8")
    token = _request(f"{login_url}/services/oauth2/token", data=token_body,
                     headers={"Content-Type": "application/x-www-form-urlencoded"})
    api_version = os.environ.get("SALESFORCE_API_VERSION", "v60.0")
    query = urllib.parse.quote("SELECT Id, Name, Industry, SystemModstamp FROM Account LIMIT 100")
    url = f"{token['instance_url']}/services/data/{api_version}/query?q={query}"
    result = _request(url, headers={"Authorization": f"Bearer {token['access_token']}"})
    records = list(result.get("records", []))
    while not result.get("done", True) and result.get("nextRecordsUrl"):
        result = _request(f"{token['instance_url']}{result['nextRecordsUrl']}",
                          headers={"Authorization": f"Bearer {token['access_token']}"})
        records.extend(result.get("records", []))
    return records


def handler(event, context):
    secret = json.loads(secrets.get_secret_value(SecretId=os.environ["SALESFORCE_SECRET_ARN"])["SecretString"])
    records = _salesforce_records(secret)
    now = datetime.now(timezone.utc).isoformat()
    table = dynamodb.Table(os.environ["SALESFORCE_TABLE"])

    for record in records:
        table.put_item(Item={
            "PK": f"SF#Account#{record['Id']}",
            "SK": f"SNAPSHOT#{now}",
            "name": record.get("Name", ""),
            "industry": record.get("Industry", ""),
            "modifiedAt": record.get("SystemModstamp", ""),
            "payload": json.dumps(record, separators=(",", ":")),
        })

    date_prefix = now[:10].replace("-", "/")
    object_key = f"salesforce/accounts/{date_prefix}/{context.aws_request_id}.json"
    s3.put_object(Bucket=os.environ["ARCHIVE_BUCKET"], Key=object_key,
                  Body=json.dumps({"fetchedAt": now, "records": records}).encode("utf-8"),
                  ContentType="application/json")
    print(json.dumps({"event": "salesforce.sync.completed", "count": len(records), "archiveKey": object_key}))
    return {"count": len(records), "archiveKey": object_key}
