import test from "node:test";
import assert from "node:assert/strict";

import { mapGraphToFlow } from "./graphMapper.mjs";

test("maps backend contacts into React Flow nodes", () => {

    const result = mapGraphToFlow({
        nodes: [
            { id: 1, fullName: "John Smith" },
            { id: 2, fullName: "Sarah Adams" }
        ],
        edges: []
    });

    assert.equal(result.nodes.length, 2);
    assert.equal(result.edges.length, 0);

    assert.equal(result.nodes[0].id, "1");
    assert.equal(result.nodes[0].data.label, "John Smith");

    assert.equal(typeof result.nodes[0].position.x, "number");
    assert.equal(typeof result.nodes[0].position.y, "number");
});

test("maps relationships with correct direction", () => {

    const result = mapGraphToFlow({
        nodes: [
            { id: 1, fullName: "John" },
            { id: 2, fullName: "Sarah" }
        ],
        edges: [
            {
                id: 10,
                sourceContactId: 1,
                targetContactId: 2,
                relationType: "MANAGES"
            }
        ]
    });

    assert.equal(result.edges.length, 1);

    assert.equal(result.edges[0].source, "1");
    assert.equal(result.edges[0].target, "2");
    assert.equal(result.edges[0].label, "MANAGES");

    assert.equal(result.relationshipCount, 1);
});

test("groups multiple relationship types for the same pair", () => {

    const result = mapGraphToFlow({
        nodes: [
            { id: 1, fullName: "John" },
            { id: 2, fullName: "Sarah" }
        ],
        edges: [
            {
                id: 10,
                sourceContactId: 1,
                targetContactId: 2,
                relationType: "MANAGES"
            },
            {
                id: 11,
                sourceContactId: 1,
                targetContactId: 2,
                relationType: "MENTORS"
            }
        ]
    });

    assert.equal(result.edges.length, 1);

    assert.equal(
        result.edges[0].label,
        "MANAGES / MENTORS"
    );

    assert.equal(result.relationshipCount, 2);
});

test("keeps reverse relationships separate", () => {

    const result = mapGraphToFlow({
        nodes: [
            { id: 1, fullName: "John" },
            { id: 2, fullName: "Sarah" }
        ],
        edges: [
            {
                id: 10,
                sourceContactId: 1,
                targetContactId: 2,
                relationType: "MANAGES"
            },
            {
                id: 11,
                sourceContactId: 2,
                targetContactId: 1,
                relationType: "KNOWS"
            }
        ]
    });

    assert.equal(result.edges.length, 2);

    assert.ok(
        result.edges.some(edge =>
            edge.source === "1" && edge.target === "2"
        )
    );

    assert.ok(
        result.edges.some(edge =>
            edge.source === "2" && edge.target === "1"
        )
    );
});

test("includes contacts without relationships", () => {

    const result = mapGraphToFlow({
        nodes: [
            { id: 1, fullName: "John" },
            { id: 2, fullName: "Sarah" },
            { id: 3, fullName: "David" }
        ],
        edges: [
            {
                id: 10,
                sourceContactId: 1,
                targetContactId: 2,
                relationType: "MANAGES"
            }
        ]
    });

    assert.equal(result.nodes.length, 3);

    assert.ok(
        result.nodes.some(node => node.id === "3")
    );
});

test("rejects invalid graph responses", () => {

    assert.throws(
        () => mapGraphToFlow({ nodes: null, edges: [] }),
        /Invalid graph data/
    );

    assert.throws(
        () => mapGraphToFlow({ nodes: [], edges: null }),
        /Invalid graph data/
    );
});

test("returns an empty graph correctly", () => {

    const result = mapGraphToFlow({
        nodes: [],
        edges: []
    });

    assert.deepEqual(result.nodes, []);
    assert.deepEqual(result.edges, []);
    assert.equal(result.relationshipCount, 0);
});