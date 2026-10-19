import Header from "./components/Header";
import ContactDirectory from "./components/ContactDirectory";
import "./App.css";
import RelationshipManager from "./components/RelationshipManager";
import {useState } from "react";
import RelationshipGraph from "./components/RelationshipGraph";

export default function App() {

  const appName = "ContactHub";

  const [graphVersion, setGraphVersion] = useState(0);

  function refreshGraph() {
    setGraphVersion(previous => previous + 1);
  }

  return (
    <div className="app-shell">

      <aside className="sidebar">
        <div className="brand">
            {appName}
            <span className="brand-dot">.</span>
        </div>


        <p className="sidebar-label">WORKFORCE</p>

        <nav aria-label="Main navigation">
            <div className="nav-item active">
              Overview
            </div>

            <div className="nav-item">
              Contacts - coming soon
            </div>


            <div className="nav-item">
              Relationships - coming soon 
            </div>
        </nav>

        <p className="sidebar-footer">
          Built with Java + React 
        </p>
      </aside>


      <main className="main-content">

        <Header
            title="Your contact workplace"
            subtitle="Manage contacts and visualize their connections."
        />


        <section className="welcome-card">
          <span className="section-label">
            PROJECT OVERVIEW 
          </span>


          <h2>
            Your backend is ready.
            Now lets build the inteface.
          </h2>

          <p>
            ContactHub will help users manage
            contacts, search records, and explore Relationships through aninteractive graph.
          </p>
        </section>


        <section className="feature-grid">
          <article className="feature-card">
            <span className="feature-icon">01</span>
            <h3>Contact Directory</h3>
            <p>
              View, search, add, edit and delete contacts.
            </p>

            <span className="feature-status">
              Coming next
            </span>

          </article>

          <article className="feature-card">
            <span className="feature-icon">02</span>
            <h3>Relationship Network</h3>

            <p>
              Visualize how your contacts are connected
            </p>

            <span className="feature-status">
              Coming soon
            </span>
          </article>
        </section>

        <ContactDirectory onChange={refreshGraph} />
        <RelationshipManager 
            onCreated={refreshGraph}
        />
        <RelationshipGraph refreshKey={graphVersion}/>
      </main>
    </div>    
  );
  
}