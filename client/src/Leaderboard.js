import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useEffect, useState } from 'react';
import './Leaderboard.css';

export const Leaderboard = () => {
    const [leaderboard, setLeaderboard] = useState([]);
    useEffect(() => {
        const fetchLeaderboard = async () => {
          try {
            const response = await fetch('http://localhost:8081/api/v1/leaderboard/daily?top=100');
            if (!response.ok) {
              throw new Error('Failed to fetch leaderboard');
            }
            const data = await response.json();
            setLeaderboard(data.data.leaderboard);
          } catch (error) {
            console.error('Error fetching leaderboard:', error);
          }
        };
    
        // Fetch initial leaderboard data when the component mounts
        fetchLeaderboard();
      }, []);
    const client = new Client({
        brokerURL: 'ws://localhost:8081/ws',
        onConnect: () => {
            client.subscribe('/topic/leaderboard', (message) => {
                const msg = JSON.parse(message.body);
                setLeaderboard(msg.leaderboard);
            }
            );
        },
    });
    client.activate();
    return (<div className="leaderboard-container">
        <h2>Leaderboard</h2>
        <table className="leaderboard-table">
            <thead>
                <tr>
                    <th>Username</th>
                    <th>Steps</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>
                {leaderboard.map((entry, index) => (
                    <tr key={index} className="leaderboard-row">
                        <td className="leaderboard-cell">{entry.username}</td>
                        <td className="leaderboard-cell">{entry.steps}</td>
                        <td className="leaderboard-cell">{new Date(entry.timestamp).toLocaleString()}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    </div>);
    

}

