
import { useState } from "react";
import Header from "./components/Header";
import ContactDirectory from "./components/ContactDirectory";
import RelationshipManager from "./components/RelationshipManager";
import RelationshipGraph from "./components/RelationshipGraph";
import "./App.css";

const navigation = [
  { id: "overview", label: "Overview", icon: "◫" },
  { id: "contacts", label: "Contacts", icon: "♧" },
  { id: "relationships", label: "Create Relationships", icon: "↗" },
  { id: "relationship-graph", label: "Relationship Graph", icon: "◎" },
];

export default function App() {
  const appName = "ContactHub";
  const [graphVersion, setGraphVersion] = useState(0);
  const [activeSection, setActiveSection] = useState("overview");

  function refreshGraph() {
    setGraphVersion(previous => previous + 1);
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a
          href="#overview"
          className="brand"
          onClick={() => setActiveSection("overview")}
        >
          <span className="brand-icon">C</span>
          <span>
            {appName}
            <span className="brand-dot">.</span>
          </span>
        </a>

        <div className="sidebar-workspace">
          <span className="workspace-indicator" />
          <span>WORKSPACE</span>
        </div>

        <p className="sidebar-label">MANAGEMENT</p>

        <nav className="sidebar-nav" aria-label="Main navigation">
          {navigation.map(item => (
            <a
              key={item.id}
              href={`#${item.id}`}
              onClick={() => setActiveSection(item.id)}
              className={`nav-item ${
                activeSection === item.id ? "active" : ""
              }`}
              aria-current={
                activeSection === item.id ? "location" : undefined
              }
            >
              <span className="nav-icon" aria-hidden="true">
                {item.icon}
              </span>
              <span>{item.label}</span>
              {activeSection === item.id && (
                <span className="nav-indicator" />
              )}
            </a>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="sidebar-tip">
            <span className="tip-icon">✦</span>
            <p>
              <strong>Stay connected</strong>
              <span>Build stronger relationships with ContactHub.</span>
            </p>
          </div>

          <div className="sidebar-footer">
            <span className="footer-status" />
            Built with Java + React
          </div>
        </div>
      </aside>

      <main className="main-content">
        <div id="overview" className="overview-section">
          <Header
            title="Your contact workplace"
            subtitle="Manage your contacts and discover meaningful connections."
          />

          <section className="welcome-card">
            <div className="welcome-copy">
              <span className="section-label">
                YOUR WORKSPACE
              </span>

              <h2>
                Every connection
                <br />
                starts with a conversation.
              </h2>

              <p>
                Organize your contacts, manage relationships, and
                explore your network — all in one place.
              </p>

              <a
                className="welcome-action"
                href="#contacts"
                onClick={() => setActiveSection("contacts")}
              >
                Explore contacts <span aria-hidden="true">→</span>
              </a>
            </div>

            <div className="welcome-art" aria-hidden="true">
              <div className="art-orbit orbit-one" />
              <div className="art-orbit orbit-two" />
              <div className="art-node node-one">C</div>
              <div className="art-node node-two">J</div>
              <div className="art-node node-three">A</div>
              <div className="art-node node-four">M</div>
              <div className="art-center">✦</div>
            </div>
          </section>
        </div>

        <ContactDirectory onChange={refreshGraph} />

        <section id="relationships" className="content-section">
          <RelationshipManager onCreated={refreshGraph} />
        </section>

       
          <RelationshipGraph refreshKey={graphVersion} />
       

        <footer className="app-footer">
          <span>ContactHub</span>
          <span>Built to bring people together.</span>
        </footer>
      </main>
    </div>
  );
}
