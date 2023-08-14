import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';


const root = ReactDOM.createRoot(document.getElementById('root'));
const client = new Client({
  brokerURL: 'ws://localhost:8081/ws',
  onConnect: () => {
    client.subscribe('/topic/leaderboard', message =>
      console.log(`Received: ${message.body}`)
    );
  },
});

client.activate();

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
