# Server-side Room Join/Leave Patterns

Here is a clear and modern example of how to handle **server-side room join/leave patterns** in a Socket.IO application, based on common real-world use cases such as chat or game lobbies.[1][2][3][5]

### Server-side Implementation

```javascript
const { Server } = require('socket.io');
const io = new Server(3000, {
  cors: { origin: "*" }
});

function logClientsInRoom(roomName) {
  const clients = Array.from(io.sockets.adapter.rooms.get(roomName) || []);
  console.log(`Clients in ${roomName}:`, clients);
}

io.on('connection', socket => {
  console.log('User connected:', socket.id);

  // Join a room
  socket.on('joinRoom', data => {
    const { roomName, userName } = data;
    socket.join(roomName); // Add the socket to that room
    console.log(`${userName} joined ${roomName}`);
    io.to(roomName).emit('userJoined', { userName, id: socket.id });
    logClientsInRoom(roomName);
    socket.emit('roomJoined', { roomName });
  });

  // Leave a room
  socket.on('leaveRoom', data => {
    const { roomName, userName } = data;
    socket.leave(roomName); // Removes socket from the room
    console.log(`${userName} left ${roomName}`);
    io.to(roomName).emit('userLeft', { userName, id: socket.id });
    logClientsInRoom(roomName);
    socket.emit('roomLeft', { roomName });
  });

  // Send message within a room
  socket.on('message', ({ roomName, text }) => {
    io.to(roomName).emit('message', { text, sender: socket.id });
  });

  // Handle disconnect events safely
  socket.on('disconnecting', () => {
    // Access rooms the socket is still part of before disconnect
    for (const room of socket.rooms) {
      if (room !== socket.id) {
        io.to(room).emit('userLeft', { id: socket.id });
      }
    }
  });
});
```

### Client-side Example

```javascript
const socket = io('http://localhost:3000');

// Join a room
socket.emit('joinRoom', { roomName: 'room1', userName: 'Alice' });

// Listen for updates
socket.on('userJoined', data => console.log(`${data.userName} joined!`));
socket.on('userLeft', data => console.log(`${data.userName} left!`));

// Leave room
socket.emit('leaveRoom', { roomName: 'room1', userName: 'Alice' });
```

### Key Patterns

- **Join rooms dynamically:** `socket.join(roomName)` ensures users are grouped logically at runtime.[3][1]
- **Leave rooms cleanly:** `socket.leave(roomName)` helps keep memory usage and broadcast lists optimal.[2][5]
- **Emit room events:** `io.to(roomName).emit('event', data)` targets all sockets in that room.[1][3]
- **Track users:** You can inspect membership via `io.sockets.adapter.rooms.get(roomName)`.[5]

This pattern efficiently scales from chat systems to real-time multiplayer games requiring grouped communication channels.

[1](https://app.studyraid.com/en/read/11916/379681/joining-and-leaving-rooms)
[2](https://stackoverflow.com/questions/34909323/socket-io-how-to-correctly-join-and-leave-rooms)
[3](https://socket.io/docs/v3/rooms/)
[4](https://docs.colyseus.io/server/room)
[5](https://github.com/socketio/socket.io/discussions/4663)
[6](https://learn.microsoft.com/en-us/javascript/api/overview/azure/web-pubsub-client-readme?view=azure-node-latest)
[7](https://dev.to/okmttdhr/micro-frontends-patters-13-server-side-composition-1of5)
[8](https://gist.github.com/crtr0/2896891)