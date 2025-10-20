# Namespaces and Rooms

In Socket.IO, **namespaces** and **rooms** are organizational features that help manage message broadcasting and isolate communication channels efficiently.[1][2][3]

### Namespaces
A **namespace** defines a logical communication channel (like a subpath) under the same connection, separating parts of an application while reusing a single WebSocket.[2][4]

**Key concepts:**
- Created using `io.of('/namespace')` on the server.
- Clients connect using `io('/namespace')`.
- Used to separate application logic, such as admin dashboards, chat features, or analytics.[4][1]

**Example:**
```javascript
// Server
const io = require('socket.io')(3000);
const chatNamespace = io.of('/chat');
chatNamespace.on('connection', socket => {
  console.log('Client joined chat namespace');
  socket.emit('welcome', 'Welcome to Chat!');
});

// Client
const socket = io('/chat');
socket.on('welcome', data => console.log(data));
```
Namespaces help in **multiplexing**—running different communication modules over one underlying connection while keeping them isolated.[5][4]

### Rooms
A **room** is a smaller, server-side group within a namespace, allowing messages to target specific clients.[3][6]

**Key points:**
- Created dynamically with `socket.join('roomName')`.
- Clients can belong to multiple rooms.
- Used to send events to subsets of users, like chat rooms or private sessions.[7][3]

**Example:**
```javascript
io.on('connection', socket => {
  socket.on('joinRoom', roomName => {
    socket.join(roomName);
    io.to(roomName).emit('message', `A new user joined ${roomName}`);
  });

  socket.on('message', ({ room, text }) => {
    io.to(room).emit('message', text);
  });
});
```
Broadcasting options :[7]
- `io.to(room).emit(event, data)` → Send to all in a room.
- `socket.to(room).emit(event, data)` → Send to all in a room except sender.
- `io.to(['room1', 'room2']).emit(event, data)` → Target multiple rooms.

### Comparison

| Feature | Namespace | Room |
|----------|------------|------|
| Scope | Application-level separation | Subdivision within a namespace |
| Client awareness | Client connects directly | Client unaware (managed by server) |
| Creation | Defined in code | Dynamic, via join/leave |
| Use case | Logical modules like “/chat” or “/admin” | Chat rooms, groups, notifications |
| Connection overhead | Shared WebSocket | Same connection reused via groups | [1][2][3]

### Practical Advice
- Use **namespaces** when components must be isolated (e.g., user vs admin dashboards).
- Use **rooms** when segmenting users dynamically for broadcasting (e.g., per chatroom or project session).  
Rooms provide lightweight message routing, while namespaces define distinct application layers.[1][4][7]

[1](https://stackoverflow.com/questions/10930286/socket-io-rooms-or-namespacing)
[2](https://socket.io/docs/v4/namespaces/)
[3](https://socket.io/docs/v4/rooms/)
[4](https://coloringchaos.github.io/rtw-s17/namespaces-rooms/)
[5](https://socket.io/docs/v3/namespaces/)
[6](https://socket.io/docs/v3/rooms/)
[7](https://www.videosdk.live/developer-hub/socketio/socketio-rooms)
[8](https://dev.to/wpreble1/socket-io-namespaces-and-rooms-d5h)
[9](https://socket.io/docs/v3/broadcasting-events/)
[10](https://www.tutorialspoint.com/socket.io/socket.io_broadcasting.htm)
[11](https://www.reddit.com/r/node/comments/4nxzye/need_help_socketio_rooms_vs_namespaces/)
[12](https://www.youtube.com/watch?v=ZKEqqIO7n-k)
[13](https://www.youtube.com/watch?v=bxUlKDgpbWs)
[14](https://stackoverflow.com/questions/53585317/how-to-broadcast-message-to-everybody-in-the-room-with-socket-io)
[15](https://socket.io/docs/v4/tutorial/step-5)
[16](https://ably.com/topic/socketio)
[17](https://socket.io/docs/v2/rooms/)