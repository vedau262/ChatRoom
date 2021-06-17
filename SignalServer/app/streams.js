module.exports = function() {
  /**
   * available streams 
   * the id value is considered unique (provided by socket.io)
   */
  var streamList = [];

  /**
   * Stream object
   */
  var Stream = function(id, name, room) {
    this.name = name;
    this.id = id;
    this.room = room;
  }

  return {
    addStream : function(id, name, room) {
      var stream = new Stream(id, name, room);
      streamList.push(stream);
    },

    removeStream : function(id) {
      var index = 0;
      while(index < streamList.length && streamList[index].id != id){
        index++;
      }
      streamList.splice(index, 1);
    },

    // update function
    update : function(id, name, room) {
      var stream = streamList.find(function(element, i, array) {
        return element.id == id;
      });
      stream.name = name;
      stream.room = room;
    },

    getStreams : function() {
      return streamList;
    }
  }
};



// module.exports = function() {
//   /**
//    * available streams 
//    * the id value is considered unique (provided by socket.io)
//    */
//   var streamList = [];

//   /**
//    * Stream object
//    */
//   var Stream = function(id, name) {
//     this.name = name;
//     this.id = id;
//   }

//   return {
//     addStream : function(id, name) {
//       var stream = new Stream(id, name);
//       streamList.push(stream);
//     },

//     removeStream : function(id) {
//       var index = 0;
//       while(index < streamList.length && streamList[index].id != id){
//         index++;
//       }
//       streamList.splice(index, 1);
//     },

//     // update function
//     update : function(id, name) {
//       var stream = streamList.find(function(element, i, array) {
//         return element.id == id;
//       });
//       stream.name = name;
//     },

//     getStreams : function() {
//       return streamList;
//     }
//   }
// };
