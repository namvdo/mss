import { Client } from "@stomp/stompjs";

const client = new Client(
    {
        brokerURL: "ws://localhost:8081/mss",
        onConnect: () => {
            client.subscribe('/topic/leaderboard', message =>
                console.log(`Received: ${message.body}`)
            );
        },
    }
)
console.log("WebSocket client");
client.activate();