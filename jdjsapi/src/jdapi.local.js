//Requires CORS Polyfill for IE8-9

(function($){
  /*jshint browser: true, devel: true, debug: true, evil: true, forin: false, undef: true, bitwise: true, eqnull: true, noarg: true, noempty: true, eqeqeq: true, boss: true, loopfunc: true, laxbreak: true, strict: true, curly: false, nonew: true, jquery: true */
  "use strict";
  
  var LocalTransport = function(options){
    this.options = $.extend({},this.defaults,options);

    if(!$.support.cors && console)
      console.warn("Warning: No CORS Support detected in your browser. Transport will fail if you are not on localhost.");
  };
    
  $.extend(LocalTransport.prototype, {
    /* default options */
    defaults: {
      apiRoot: "http://localhost:3128",
      timeout: 31*1000 //ms
    },
    connect: function(){
      var handshake = $.Deferred();
      var auth = $.ajax({
        type: "POST",
        url: this.options.apiRoot + "/session/connect",
        data: JSON.stringify([this.options.user, this.options.pass]),
        dataType: "json",
        timeout: this.options.timeout
      });
            
      //if authentication fails, reject the handshake
      auth.fail(handshake.reject.bind(handshake));
      //if the handshake gets rejected beforehand (e.g. because of a disconnect() call),
      //abort the authentication request
      handshake.fail(auth.abort.bind(auth));
      
      auth.done((function(data){
        if(!data.token)
          return handshake.reject("No error thrown, but no token returned");
        this.initialiseConnection(data.token);
        handshake.resolve(this.connection);
      }).bind(this));
      
      return handshake;
    },
    initialiseConnection: function(token){
      this.options.token = token;
      
      var self = this;
      this.connection = $.Deferred();
      this.connection.always(function(){
        self.disconnect();
      });
      return this.connection;
    },
    disconnect: function(){
      return this.send("/session/disconnect")
        .done(this.connection.resolve.bind(this.connetion))
        .fail(this.connection.reject.bind(this.connetion));
    },
    listen: function(){
      
      var listener = $.Deferred();
      
      listener.always(function(){
        if(listener._request)
          listener._request.abort();
      });
      
      this._listen(listener);
      
      return listener;
    },
    _listen: function(listener){
      if(listener.state() === "pending") {
        var request = this.send("/events/listen");
        request.done(listener.notify.bind(listener));
        request.done(this._listen.bind(this,listener));
        request.fail(listener.reject.bind(listener));
        
        listener._request = request;
        return request;
      }
    },
    send: function(action, params){
      
      var request = $.ajax({
        type: "POST",
        url: this.options.apiRoot + action + "?" + $.param({token: this.options.token}),
        data: JSON.stringify(params),
        dataType: "json",
        timeout: this.options.timeout
      });
      return request;
    },
    getAuth: function(){
      if(this.options.user && this.options.pass)
        return {
          user: this.options.user,
          pass: this.options.pass
        };
      return false;
    }
  });
  
  window.jd = window.jd || {};
  window.jd.LocalTransport = LocalTransport;
  
})(jQuery);