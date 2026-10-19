import dagre from "@dagrejs/dagre";
import { MarkerType, Position } from "@xyflow/react";

const NODE_WIDTH = 180;
const NODE_HEIGHT = 60;

export function mapGraphToFlow(graphData) {

    if (
        !Array.isArray(graphData?.nodes) ||
        !Array.isArray(graphData?.edges)
    ) {
        throw new Error("Invalid graph data returned by the API.");
    }

    const layoutGraph = new dagre.graphlib.Graph();

    layoutGraph.setDefaultEdgeLabel(() => ({}));

    layoutGraph.setGraph({
        rankdir: "TB",
        nodesep: 75,
        ranksep: 120,
        marginx: 40,
        marginy: 40
    });

    const contactIds = new Set(
        graphData.nodes.map(contact => String(contact.id))
    );

    // Add all contacts, including isolated ones.
    for (const contact of graphData.nodes) {

        layoutGraph.setNode(String(contact.id), {
            width: NODE_WIDTH,
            height: NODE_HEIGHT
        });
    }

    // Group multiple relationship types for the same direction.
    const groupedRelationships = new Map();

    for (const relationship of graphData.edges) {

        const source = String(relationship.sourceContactId);
        const target = String(relationship.targetContactId);

        // Ignore relationships referencing missing graph nodes.
        if (!contactIds.has(source) || !contactIds.has(target)) {
            continue;
        }

        const key = `${source}->${target}`;

        if (!groupedRelationships.has(key)) {

            groupedRelationships.set(key, {
                source,
                target,
                types: new Set()
            });
        }

        groupedRelationships
            .get(key)
            .types
            .add(relationship.relationType);
    }

    for (const relation of groupedRelationships.values()) {

        layoutGraph.setEdge(relation.source, relation.target);
    }

    dagre.layout(layoutGraph);

    const nodes = graphData.nodes.map(contact => {

        const layout = layoutGraph.node(String(contact.id));

        return {
            id: String(contact.id),

            position: {
                x: layout.x - NODE_WIDTH / 2,
                y: layout.y - NODE_HEIGHT / 2
            },

            data: {
                label: contact.fullName
            },

            sourcePosition: Position.Bottom,
            targetPosition: Position.Top
        };
    });

    const edges = Array.from(
        groupedRelationships.entries()
    ).map(([key, relation]) => ({

        id: `relationship-${key}`,

        source: relation.source,
        target: relation.target,

        label: Array.from(relation.types).join(" / "),

        type: "smoothstep",

        markerEnd: {
            type: MarkerType.ArrowClosed
        },

        style: {
            stroke: "#64748b",
            strokeWidth: 2
        },

        labelBgStyle: {
            fill: "#ffffff"
        },

        labelBgPadding: [8, 4],
        labelBgBorderRadius: 6
    }));

    return {
        nodes,
        edges,
        relationshipCount: graphData.edges.length
    };
}