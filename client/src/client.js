import { Stomp } from "@stomp/stompjs";

const client = Stomp.over("ws://localhost:8081/ws");
client.onConnect = () => {
    console.log("connected");
}