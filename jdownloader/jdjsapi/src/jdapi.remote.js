//Requires CryptoJS
//Requires Same Domain Proxy for IE8-9

//XDomainRequest isnt sufficient because we have custom request/response headers.
(function($){
  /*jshint browser: true, devel: true, debug: true, evil: true, forin: false, undef: true, bitwise: true, eqnull: true, noarg: true, noempty: true, eqeqeq: true, boss: true, loopfunc: true, laxbreak: true, strict: true, curly: false, nonew: true, jquery: true */
  /*global CryptoJS */
  "use strict";
  
  //Extend CryptoJS to return halfes of the token.
  CryptoJS.lib.WordArray.firstHalf = function(){
    if(!this._firstHalf) {
      this._firstHalf = new CryptoJS.lib.WordArray.init(this.words.slice(0, this.words.length/2));
    }
    return this._firstHalf;
  };
  CryptoJS.lib.WordArray.secondHalf = function(){
    if(!this._secondHalf)
      this._secondHalf = new CryptoJS.lib.WordArray.init(this.words.slice(this.words.length/2, this.words.length));
    return this._secondHalf;
  };
  var transfer_encoding = CryptoJS.enc.Hex;
  
  /**
   *   Initialize the remote transport with the given options.
   *   If you are authenticating for the first time, user and pass are mandatory.
   *   If you are reauthenticating, pass the .getAuth() object here.
   */
  var RemoteTransport = function(options){
    this.options = $.extend({},this.defaults,options);

    if(!$.support.cors && console)
      console.warn("Warning: No CORS Support detected in your browser. Transport will most likely fail.");
  };
    
  $.extend(RemoteTransport.prototype, {
    /* default options */
    defaults: {
      apiRoot: "http://api.jdownloader.org:10101",
      timeout: 31*1000 //ms
    },
    /* hash a password with the given pass and domain as salt */
    hashPassword: function(user, pass, domain){
      return CryptoJS.SHA256( CryptoJS.enc.Utf8.parse( user + pass + domain ) );
    },
    /* convert options.pass to secret hashes and delete it afterwards */
    _processPassword: function(){
      if(!this.options.user || !this.options.pass)
        return;
      
      console && console.time("generate Secrets");
      this.options.secretServer = this.hashPassword(this.options.user, this.options.pass,"server");
      this.options.secretJD     = this.hashPassword(this.options.user, this.options.pass,"jd");
      console && console.timeEnd("generate Secrets");
      
      delete this.options.pass;
    },
    /* get secret for the specified domain */
    getSecret: function(res){
      res = "secret"+res;
      if(res in this.options) {
        if(typeof(this.options[res]) === "string")
          this.options[res] = CryptoJS.enc.Hex.parse(this.options[res]);
        return this.options[res];
      }
      if(this.options.pass) {
        this._processPassword();
        return this.options[res];
      }
    },
    /* Connect to the API, either with username or regaintoken */
    connect: function(){
      
      if(!this.getSecret("Server"))
        return $.Deferred().reject("no credentials given");
      
      //craft query string
      var params = {};
      var action;
      if(this.options.regainToken) {
        params.regainToken = this.options.regainToken;
        action = "clientreconnect";
      } else {
        if(!this.options.user)
          return $.Deferred().reject("no credentials given");
        params.user = this.options.user;
        action = "clientconnect";
      }
      
      params.timestamp = (new Date()).getTime();
      var queryString = "/my/"+action+"?" + $.param(params);
      
      queryString += "&signature=" + 
        CryptoJS.HmacSHA256( CryptoJS.enc.Utf8.parse(queryString) , this.options.secretServer).toString(transfer_encoding);
      
      //issue authentication request
      var auth = $.ajax({
        url: this.options.apiRoot + queryString,
        type: "POST",
        dataType: "aesjson-server",
        converters: {"* aesjson-server": this._decryptJSON.bind(this,"Server")}
      });
      var handshake = $.Deferred();
      
      //if authentication fails, reject the handshake
      auth.fail(handshake.reject.bind(handshake));
      //if the handshake gets rejected beforehand (e.g. because of a disconnect() call),
      //abort the authentication request
      handshake.fail(auth.abort.bind(auth));
      
      //if authentication is successfull, initialize connection
      auth.done((function(data){
        if(data.timestamp !== params.timestamp)
          return handshake.reject(undefined, "replay attack");
        this.initialiseConnection(data.token, data.regainToken);
        handshake.resolve(this.connection);
      }).bind(this));
      
      return handshake;
    },
    /* initialize the connection after a successful handshake */
    initialiseConnection: function(token, regainToken){
      this.options.token = token;
      this.options.regainToken = regainToken;
      this.tokenRoot = this.options.apiRoot + "/" + this.options.token;
      var secretJDToken_raw = new CryptoJS.lib.WordArray.init([],0);
      secretJDToken_raw.concat(this.getSecret("JD"));
      secretJDToken_raw.concat(CryptoJS.enc.Hex.parse(this.options.token));
      this.options.secretJDToken = CryptoJS.SHA256( secretJDToken_raw );
      
      this.connection = $.Deferred();
      this.connection.always(this.disconnect.bind(this));
      return this.connection;
    },
    /* disconnect from the server, invalidate the active token */
    disconnect: function(){
      
      var params = {
        clienttoken: this.options.token,
        timestamp: (new Date()).getTime()
      };
      var queryString = "/my/disconnect?" + $.param(params);
      
      queryString += "&signature=" + 
        CryptoJS.HmacSHA256( CryptoJS.enc.Utf8.parse(queryString) , this.options.secretServer).toString(transfer_encoding);
      
      return $.ajax({
        url: this.options.apiRoot + queryString,
        type: "POST",
        dataType: "aesjson-server",
        converters: {"* aesjson-server": this._decryptJSON.bind(this,"Server")}
      })
      .done((function(data){
        if(data.timestamp !== params.timestamp)
          return this.connection.reject(undefined,"replay attack");
        this.connection.resolve("disconnected");
      }).bind(this)).fail(this.connection.reject.bind(this.connection));
    },
    _encryptJSON: function(secretId, plain){
      var iv  = this.getSecret(secretId).firstHalf();
      var key = this.getSecret(secretId).secondHalf();
      
      console && console.time("encryptJSON: "+plain.length);
      var encrypted = CryptoJS.AES.encrypt(JSON.stringify(plain), key , {mode: CryptoJS.mode.CBC, iv:iv});
      console && console.timeEnd("encryptJSON: "+plain.length);
      
      return encrypted.toString();
    },
    _decryptJSON: function(secretId, encrypted){
      var iv  = this.getSecret(secretId).firstHalf();
      var key = this.getSecret(secretId).secondHalf();
      
      console && console.time("decryptJSON: "+encrypted.length);
      var plain_raw = CryptoJS.AES.decrypt(encrypted, key, {mode: CryptoJS.mode.CBC, iv:iv}).toString(CryptoJS.enc.Utf8);
      var plain = JSON.parse(plain_raw);
      console && console.timeEnd("decryptJSON: "+encrypted.length);
      
      return plain;
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
        var request = $.ajax(
          {
            type: "GET",
            url: this.tokenRoot + "/events/listen",
            timeout: this.options.timeout,
            dataType: "aesjson-jd",
            converters: {"* aesjson-jd": this._decryptJSON.bind(this,"JDToken")}
          });
        request.done(listener.notify.bind(listener));
        request.done(this._listen.bind(this,listener));
        request.fail(listener.reject.bind(listener));
        listener._request = request;
        return request;
      }
    },
    send: function(action, params){
      
      var data = {
        url: action,
        params: params,
        timestamp: (new Date()).getTime()
      };
      
      var request = $.ajax({
        type: "POST",
        url: this.tokenRoot + action,
        contentType: "application/aesjson-jd",
        data: this._encryptJSON("JDToken",data),
        timeout: this.options.timeout,
        dataType: "aesjson-jd",
        converters: {"* aesjson-jd": this._decryptJSON.bind(this,"JDToken")}
      });
      
      var filtered = $.Deferred(); //filtered response deferred
      
      request.done(filtered.resolve.bind(filtered));
      //If we receive a response from the server instead of JD, 
      //we decrypt it before passing it to the handler.
      //Note: Getting a request from the server instead of JD implies that there was an error.
      request.fail(function(jqXHR){

        var ct = jqXHR.getResponseHeader("content-type");
        if(ct)
          ct = /aesjson-(server|jd)/.exec(ct)[1];
        if(ct) {
          //we received a jd/server response, decrypt it.
          try {
            var decrypted = this._decryptJSON(ct === "jd" ? "JDToken":"Server", jqXHR.responseText);
            filtered.reject(jqXHR, ct+"response", decrypted);
          } catch(e) {
            filtered.reject(jqXHR, "parsererror", e);
          }
        } else {
          filtered.reject.apply(filtered,arguments);
        }
      });
      
      return filtered;
    },
    getAuth: function(){
      if(this.options.regainToken && this.getSecret("JD"))
        return {
          regainToken: this.options.regainToken,
          secretServer:  this.getSecret("Server").toString(transfer_encoding),
          secretJD:      this.getSecret("JD").toString(transfer_encoding)
        };
      //never return the pass in plaintext, return all hashes instead.
      //conversion is done by getSecret()
      if(this.getSecret("Server") && this.getSecret("JD") && this.options.user)
        return {
          user:        this.options.user,
          secretServer:  this.getSecret("Server").toString(transfer_encoding),
          secretJD:      this.getSecret("JD").toString(transfer_encoding)
        };
      return false;
    },
    /* Register a new user account on the api */
    registerUser: function(data){
      if(  !data.user 
        || !data.pass 
        || !(/.@./.test(data.mail)) 
        || !data.captcha_challenge
        || !data.captcha_response)
        throw "Invalid parameters";
      
      data.secretServer = this.hashPassword(data.user,data.pass,"server").toString(transfer_encoding);
      delete data.pass;
      
      return $.ajax({
        url: this.options.apiRoot + "/my/register", 
        type: "POST",
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8"
      });
    }
  });
  
  window.jd = window.jd || {};
  window.jd.RemoteTransport = RemoteTransport;
  
})(jQuery);