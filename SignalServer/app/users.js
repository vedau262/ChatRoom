module.exports = function() {
  /**
   * available streams 
   * the id value is considered unique (provided by socket.io)
   */
  var userList = [];

  /**
   * Stream object
   */
  var User = function(roomId, userId) {
    this.roomId = roomId;
    this.userId = userId;
  }

  return {
    addUser : function(roomId, userId) {
      var user = new User(roomId, userId);
      userList.push(user);
    },

    removeUser : function(userId) {
      var index = 0;
      while(index < userList.length && userList[index].userId != userId){
        index++;
      }
      userList.splice(index, 1);
    },

    // update function
    update : function(roomId, userId) {
      var user = userList.find(function(element, i, array) {
        return user.roomId == roomId && user.userId == userId;
      });
      user.roomId = roomId;
    },

    getUserList : function() {
      return userList;
    }
  }
};
