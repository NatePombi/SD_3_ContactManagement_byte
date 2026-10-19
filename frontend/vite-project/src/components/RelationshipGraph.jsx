import { useEffect, useState } from "react";
import { mapGraphToFlow } from "../utils/graphMapper.mjs";
import { apiUrl } from "../config/api.js";


import {
    ReactFlow,
    Background,
    Controls,
    MiniMap,
    useNodesState
} from "@xyflow/react";

import "@xyflow/react/dist/style.css";

export default function RelationshipGraph({ refreshKey = 0 }) {

    // React Flow nodes can be dragged by the user
    const [nodes, setNodes, onNodesChange] = useNodesState([]);

    // Edges represent database relationships
    const [edges, setEdges] = useState([]);

    // Request state
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // Track the selected contact
    const [selectedContactId, setSelectedContactId] = useState(null);

    // Display the number of database relationships
    const [relationshipCount, setRelationshipCount] = useState(0);

    // Allows manual refresh
    const [reloadCount, setReloadCount] = useState(0);

    useEffect(() => {

        const controller = new AbortController();

        async function loadGraph() {

            setLoading(true);
            setError("");

            try {

                const response = await fetch(apiUrl(
                    "/api/v1/relationships/graph"),
                    {
                        signal: controller.signal
                    }
                );

                if (!response.ok) {
                    throw new Error(
                        `Unable to load graph (HTTP ${response.status})`
                    );
                }

                const data = await response.json();

                console.log("Graph API response:", data);
                console.log("First contact:", data.nodes[0]);

                if (
                    !Array.isArray(data.nodes) ||
                    !Array.isArray(data.edges)
                ) {
                    throw new Error(
                        "Unexpected graph response from the server."
                    );
                }

                // Convert backend contacts into React Flow nodes
                const {
                    nodes: flowNodes,
                    edges: flowEdges,
                    relationshipCount: totalRelationships
                } = mapGraphToFlow(data);

               

                if (!controller.signal.aborted) {

                    setNodes(flowNodes);
                    setEdges(flowEdges);

                    setRelationshipCount(totalRelationships);
                    setSelectedContactId(null);
                }

            } catch (err) {

                if (!controller.signal.aborted) {
                    setError(
                        err.message || "Unable to load relationship graph."
                    );
                }

            } finally {

                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        loadGraph();

        return () => controller.abort();

    }, [refreshKey, reloadCount, setNodes]);

    const selectedNode = nodes.find(
        node => node.id === selectedContactId
    );

    return (
        <section
            className="graph-section"
            id="relationship-graph"
        >

            <div className="graph-header">

                <div>
                    <span className="section-label">
                        SIX DEGREES NETWORK
                    </span>

                    <h2>Relationship Graph</h2>

                    <p>
                        Explore how your contacts are connected.
                    </p>
                </div>

                <button
                    type="button"
                    className="secondary-button"
                    onClick={() =>
                        setReloadCount(previous => previous + 1)
                    }
                    disabled={loading}
                >
                    Refresh Graph
                </button>

            </div>

            {loading ? (

                <div className="loading-state" role="status">
                    Loading relationship graph...
                </div>

            ) : error ? (

                <div className="error-state" role="alert">

                    <p>{error}</p>

                    <button
                        type="button"
                        className="retry-button"
                        onClick={() =>
                            setReloadCount(previous => previous + 1)
                        }
                    >
                        Try Again
                    </button>

                </div>

            ) : nodes.length === 0 ? (

                <div className="empty-state">
                    No contacts available to display.
                </div>

            ) : (

                <>
                    <div className="graph-statistics">
                        <span>{nodes.length} contacts</span>
                        <span>{relationshipCount} relationships</span>
                    </div>

                    <div className="graph-container">

                        <ReactFlow
                            nodes={nodes}
                            edges={edges}
                            onNodesChange={onNodesChange}
                            onNodeClick={(_, node) =>
                                setSelectedContactId(node.id)
                            }
                            nodesConnectable={false}
                            nodesDeletable={false}
                            edgesFocusable={false}
                            deleteKeyCode={null}
                            fitView
                            fitViewOptions={{ padding: 0.2 }}
                        >

                            <Background
                                gap={20}
                                size={1}
                                color="#dce4f0"
                            />

                            <Controls />

                            <MiniMap
                                nodeColor="#8fb4e8"
                                nodeStrokeWidth={2}
                            />

                        </ReactFlow>

                    </div>

                    {selectedNode && (

                        <div className="selected-contact-panel">

                            <span className="section-label">
                                SELECTED CONTACT
                            </span>

                            <h3>{selectedNode.data.label}</h3>

                            <p>
                                Contact ID: {selectedNode.id}
                            </p>

                        </div>

                    )}

                    <p className="graph-hint">
                        Drag contacts to reposition them.
                        Use the mouse wheel to zoom and drag
                        the background to move around.
                    </p>

                </>

            )}

        </section>
    );
}