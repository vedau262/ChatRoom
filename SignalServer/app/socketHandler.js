

var ChatMessage = function(roomId, userId, userName, message) {
    this.roomId = roomId;
    this.userId = userId;
    this.userName = userName;
    this.message = message;
  }

const dataMsg = []
module.exports = function(io, streams) {
  io.on("connection", function(client) {
    console.log("-- " + client.id + " joined --");
    client.emit("id", client.id);

    client.on("message", function(details) {
      var otherClient = io.sockets.connected[details.to];
	    console.log("-- " + client.id + " message --" + details);	
      if (!otherClient) {
        return;
      }
      delete details.to;
      details.from = client.id;
      otherClient.emit("message", details);
    });

    client.on("readyToStream", function(options) {
      console.log("-- " + client.id + " is ready to stream --");
      streams.addStream(client.id, options.name, options.room);
      client.emit("id", client.id);
      io.emit("ids", streams.getStreams());	    
      console.log("-- ids" + streams.getStreams());
      
      // client.join(options.room);
    });    

    client.on("get_id", function(options) { 
	    client.emit("id", client.id);
    });  


    client.on("update", function(options) {
	    console.log("-- update ids" ,options);
      streams.update(client.id, options.name, options.room);
      client.emit("id", client.id);
	    io.emit("ids", streams.getStreams());
      
    });

    client.on("join_chat_room", function(options) {
	    console.log("-- join_chat_room" , options);
      client.join(options);
    });

    client.on("refreshids", function(options) {
      client.emit("ids", streams.getStreams());
    });

	
    client.on('test', function(data){
      client.emit('test',data);
    });

    // Listen for chat message being sent from client
    client.on('get_all_messages', function(roomId){
      console.log("-- get_all_messages for roomId: " , roomId);
      client.emit('receiver_message', dataMsg);
    });


    // Listen for chat message being sent from client
    client.on('send_message', function(data){
      // Send received chat message to all connected clients	
      console.log("-- send_message: " , data);
      var mes = new ChatMessage(data.roomId, data.userId, data.userName, data.message);
      dataMsg.push(mes)
      // io.emit('receiver_message',dataMsg);

      const mesArray = [mes]
      io.to(data.roomId).emit('receiver_message', mesArray);
      // io.emit('receiver_message', mesArray);
    });    

    function leave() {
      console.log("-- " + client.id + " left --");
      streams.removeStream(client.id);      
      io.emit("ids", streams.getStreams());
    }

    client.on("disconnect", leave);
    client.on("leave", leave);
  });
};





// module.exports = function(io, streams) {
//   io.on("connection", function(client) {
//     console.log("-- " + client.id + " joined --");
//     client.emit("id", client.id);

//     client.on("message", function(details) {
//       var otherClient = io.sockets.connected[details.to];

//       if (!otherClient) {
//         return;
//       }
//       delete details.to;
//       details.from = client.id;
//       otherClient.emit("message", details);
//     });

//     client.on("readyToStream", function(options) {
//       console.log("-- " + client.id + " is ready to stream --");
//       streams.addStream(client.id, options.name);
//       client.emit("ids", streams.getStreams());
//       console.log("-- ids" + streams.getStreams());
//     });

//     client.on("update", function(options) {
//       streams.update(client.id, options.name);
//     });

//     client.on("refreshids", function(options) {
//       client.emit("ids", streams.getStreams());
//     });

//     function leave() {
//       console.log("-- " + client.id + " left --");
//       streams.removeStream(client.id);
//       client.emit("ids", streams);
//     }

//     client.on("disconnect", leave);
//     client.on("leave", leave);
//   });
// };
