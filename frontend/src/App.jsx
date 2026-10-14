import { useState } from 'react';

const initialEvents = [
  { id: 'EVT-1048', type: 'salesforce.sync.completed', source: 'salesforce-sync', state: 'Completed', time: '09:42:18' },
  { id: 'EVT-1047', type: 'order.created', source: 'rest-service', state: 'Processed', time: '09:38:04' },
  { id: 'EVT-1046', type: 'report.ready', source: 'reporting-service', state: 'Archived', time: '09:31:52' },
];

export default function App() {
  const [events, setEvents] = useState(initialEvents);
  const [notice, setNotice] = useState('Demo activity shown. Connect the API to send a real order.');
  const [busy, setBusy] = useState(false);

  async function createOrder() {
    setBusy(true);
    setNotice('Sending request to the REST service...');
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? ''}/api/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ source: 'react-mfe', kind: 'sample', detail: 'Khoros bridge demo order' }),
      });
      if (!response.ok) throw new Error(`API returned ${response.status}`);
      const order = await response.json();
      const now = new Date().toLocaleTimeString();
      setEvents((current) => [{ id: order.orderId ?? `ORD-${Date.now()}`, type: 'order.created', source: 'rest-service', state: 'Accepted', time: now }, ...current]);
      setNotice(`Order ${order.orderId ?? ''} accepted. Events are being processed asynchronously.`);
    } catch (error) {
      setNotice(`Could not reach the API: ${error.message}. The page is still in demo mode.`);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="Khoros Bridge home"><span className="brand-mark">K</span><span>KHOROS <b>BRIDGE</b></span></a>
        <div className="environment"><span className="pulse" /> SANDBOX <span className="divider">/</span> INTEGRATION</div>
        <button className="avatar" aria-label="Demo user">AB</button>
      </header>
      <section className="workspace" id="top">
        <aside className="rail" aria-label="Primary navigation">
          <button className="rail-item active" title="Overview" aria-label="Overview"><span>◫</span></button>
          <button className="rail-item" title="Events" aria-label="Events"><span>⇄</span></button>
          <button className="rail-item" title="Reports" aria-label="Reports"><span>▤</span></button>
          <div className="rail-bottom"><button className="rail-item" title="Settings" aria-label="Settings"><span>⚙</span></button></div>
        </aside>
        <div className="content">
          <div className="page-heading">
            <div><p className="eyebrow">OPERATIONS / OVERVIEW</p><h1>Bridge activity</h1><p className="subheading">A live view across your Salesforce integration pipeline.</p></div>
            <button className="primary-button" onClick={createOrder} disabled={busy}><span aria-hidden="true">＋</span>{busy ? 'Sending...' : 'Create sample order'}</button>
          </div>
          <div className="status-strip" role="status"><span className="status-dot" />{notice}</div>
          <section className="metrics" aria-label="Service status">
            <article className="metric"><div className="metric-label">PIPELINE HEALTH <span className="metric-icon green">✓</span></div><div className="metric-value">Operational</div><div className="metric-foot"><span className="tiny-dot" />All demo services reporting</div></article>
            <article className="metric"><div className="metric-label">EVENTS TODAY <span className="metric-icon blue">↗</span></div><div className="metric-value">1,284</div><div className="metric-foot">Across MSK and SQS</div></article>
            <article className="metric"><div className="metric-label">LAST SALESFORCE SYNC <span className="metric-icon amber">⟳</span></div><div className="metric-value">09:42 <small>AM</small></div><div className="metric-foot">Next run in 18 minutes</div></article>
            <article className="metric"><div className="metric-label">REPORT ARCHIVE <span className="metric-icon coral">▤</span></div><div className="metric-value">38 <small>files</small></div><div className="metric-foot">Stored in S3 this week</div></article>
          </section>
          <section className="activity-section">
            <div className="section-heading"><div><p className="eyebrow">MESSAGE BUS</p><h2>Recent events</h2></div><div className="feed-controls"><span className="live-label"><i /> LIVE FEED</span><button className="icon-button" title="Refresh events" aria-label="Refresh events" onClick={() => setNotice('The demo event feed is up to date.')}>↻</button></div></div>
            <div className="table-wrap"><table><thead><tr><th>EVENT</th><th>TYPE</th><th>PRODUCER</th><th>STATUS</th><th>TIME</th></tr></thead><tbody>{events.map((event) => <tr key={event.id}><td className="event-id">{event.id}</td><td><span className="event-type">{event.type}</span></td><td>{event.source}</td><td><span className={`state state-${event.state.toLowerCase()}`}>{event.state}</span></td><td className="time-cell">{event.time}</td></tr>)}</tbody></table></div>
            <div className="table-footer"><span>Showing {events.length} of 1,284 events</span><button className="text-button" onClick={() => setNotice('Full event history is available through the reporting service.')}>View event history <span aria-hidden="true">→</span></button></div>
          </section>
          <footer className="page-footer"><span>KHOROS BRIDGE <i>·</i> INTEGRATION SANDBOX</span><span>CloudWatch telemetry connected</span></footer>
        </div>
      </section>
    </main>
  );
}
