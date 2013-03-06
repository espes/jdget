(function($){
  /*jshint browser: true, devel: true, debug: true, evil: true, forin: false, undef: true, bitwise: true, eqnull: true, noarg: true, noempty: true, eqeqeq: true, boss: true, loopfunc: true, laxbreak: true, strict: true, curly: false, nonew: true, jquery: true */
  "use strict";
  
  /**
   *   Initialize the API with a given transport.
   *   The transport is responsible for communicating with the API.
   *   Currently, both a RemoteTransport for api.jdownloader.org
   *   and a LocalTransport for local connections are available.
   */
  var API = function(transport){
    this.transport = transport;
  };
  
  var not_connected = $.Deferred().reject(undefined,"not connected"); //undefined as first parameter to match jqXHR.fail
  
  $.extend(API.prototype, {
    /**
     *   The JDownloader jQuery API Wrapper makes heavy use of Deferreds.
     *   To understand this API, a basic knowledge about them is mandatory.
     *   http://api.jquery.com/category/deferred-object/
     *
     *   Deferred State Chart:
     *
     *   |--- handshake lifetime ---|
     *                              |---   connection lifetime   ---|
     *                               |--- listener lifetime ---|
     *
     *   handshake.state()  == "pending": Authentication in progress
     *   handshake.state()  == "resolved": Connection established and active
     *   handshake.state()  == "rejected": No active connection
     *   handshake.done callbacks get called when authentication has succeeded.
     *   handshake.fail callbacks get called when authentication has failed or was aborted.

     *   connection.state() == "pending": Connection established and active
     *   connection.state() == "resolved": Connection has been closed on purpose
     *   connection.state() == "rejected": No active connection
     *   connection.done callbacks get called when the connection has been closed on purpose
     *   connection.fail callbacks get called when the session couldn't be closed.
     *   
     *   listener.state() == "pending": Actively polling for requests
     *   listener.state() == "resolved": Listening for requests has been stopped on purpose.
     *   listener.state() == "rejected": Never listened for request or an error occured while listening for events.
     *   listener.done callbacks get called when listening for events has been closed on purpose
     *   listener.fail callbacks get called when there was an error while listening to events.
     *                 Call .listen() again to retry.
     *   listener.notify callbacks get triggered whenever there is a new event.
     */
    handshake: not_connected,
    connection: not_connected,
    listener: not_connected,
    /**
     *   Connect to the API server and authenticate.
     *   Subscribe to handshake.done to see when the connection
     *   has been established.
     *   @returns handshake deferred that gets resolved /rejected 
     *            when the authentication has  succeeded/failed
     */
    connect: function(){
      if(this.connection.state() === "pending") {
        return this.handshake;
      }
      
      this.handshake = this.transport.connect.apply(this.transport, arguments);
      
      var self = this;
      this.handshake.done(function(connection){
        self.connection = connection;
        self.connection.always(function(){
          self.handshake = not_connected;
        });
      });
            
      return this.handshake;
    },
    /**
     *   Disconnect from the API server.
     *   @returns a deferred that gets resolved as soon you are disconnected.
     */
    disconnect: function(){
      if(this.handshake.state() === "pending") {
        this.handshake.reject("disconnect");
      }
      if(this.connection.state() !== "pending") {
        return not_connected;
      }
      return this.transport.disconnect.apply(this.transport,arguments);
    },
    /**
     *   Start polling events from the server.
     *   Subscribe to listener.notify to receive events. 
     *   @returns the listener Deferred
     */
    listen: function(){
      if(this.connection.state() !== "pending") {
        return not_connected;
      }
      if(this.listener.state() === "pending") {
        return this.listener;
      }
      this.listener = this.transport.listen.apply(this.transport,arguments);
      
      //Cascade Connection close/fail events to the listener
      this.connection.fail(this.listener.reject.bind(this.listener));
      this.connection.done(this.listener.resolve.bind(this.listener));
      
      return this.listener;
    },
    /**
     *   Stop polling events
     */
    stopListen: function(){
      if(this.listener.state() === "pending") {
        this.listener.resolve.apply(this.listener,arguments);
      }
      return this.listener;
    },
    /**
     *   Send a message with the given params to the server.
     *   Please make sure that you specify the parameters in the correct order.
     *
     *   @param action: The desired action (e.g. "/linkgrabber/add") as a string
     *   @param params: An array of primitives containing the parameters for the action.
     *   @returns a deferred that gets resolved as soon as the action has been done.
     *   If you are interested in the results, subscribe to returnedDeferred.done
     */
    send: function(action, params){
      if(this.connection.state() !== "pending") {
        return not_connected;
      }
      
      //convert data to array if neccessary
      params = params || [];
      if($.isPlainObject(params)) {
        var _params = [];
        for (var key in params) {
          _params.push(params[key]);
        }
        params = _params;
      }
      var args = Array.prototype.slice.apply(arguments);
      args[1] = params;
      
      return this.transport.send.apply(this.transport,args);
    },
    /**
     *   @returns a dictionary (object) containing the neccessary data 
     *   for the next authentication.
     */
    getAuth: function(){
      return this.transport.getAuth();
    }
  });
  
  window.jd = window.jd || {};
  window.jd.API = API;
  
})(jQuery);