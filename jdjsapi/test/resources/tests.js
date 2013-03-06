QUnit.config.urlConfig.push({
  id: "localtransport",
  label: "LocalTransport",
  tooltip: "Use jd.LocalTransport for requests"
});
var css, cssFile, cssRule; //workaround for Chrome

var api, isLocal = !!QUnit.urlParams.localtransport;
var transport = isLocal ? jd.LocalTransport : jd.RemoteTransport;
var user = QUnit.urlParams.user || "foo",
    pass = QUnit.urlParams.pass || "bar";

api = new jd.API(new transport({
    user: user,
    pass: pass
  }));

if(!isLocal) {
  test("api aes encryption", function(){
    var plain = {foo:"bar"};
    var crypted = api.transport._encryptJSON("Server",plain);
    var decrypted = api.transport._decryptJSON("Server",crypted);
    console.log(decrypted);
    equal(plain.foo,decrypted.foo,"decrypt(encrypt(msg) == msg");
  });
}

test( "api.getAuth()", function() {
  var tmp = api.transport.options;
  api.transport.options = {};
  strictEqual(api.getAuth(),false,"dont return incomplete auth");
  api.transport.options = tmp;
  ok(!!api.getAuth(),"return complete auth");
});

function checkClosed3(){
  notEqual(api.handshake.state(),"pending","handshake is closed");
  notEqual(api.connection.state(),"pending","connection is closed");
  notEqual(api.listener.state(),"pending","listener is closed");  
}

asyncTest("api.connect()", function() {
  expect( 7 );
  checkClosed3();
  api.connect().done(function(){
    equal(api.handshake.state(),"resolved","handshake is resolved");
  	equal(api.connection.state(),"pending","connection is pending");
  }).fail(function(jqxhr,msg){
    ok( false, "connection failed: " + msg);
  }).always(start);
  equal(api.handshake.state(),"pending","handshake is in pending state");
  notEqual(api.connection.state(),"pending","connection is closed");
});


asyncTest("api.send()", function() {
  api.send("/ping").done(function(data){
    
    equal(data.pong,true,"pong received");
    
  }).fail(function(jqxhr,msg){
    ok(false,msg);
  }).always(start);
});


asyncTest("api.send() with parameters", function() {
  api.send("/ping",["foo",3,true]).done(function(data){
    
    equal(data.a,"foo","answer correct");
    equal(data.b,3,    "answer correct");
    equal(data.c,true, "answer correct");
    
  }).fail(function(jqxhr,msg){
    ok(false,msg);
  }).always(start);
});


asyncTest("api.disconnect()", function() {
  expect( 4 );
  equal(api.connection.state(),"pending","connection is pending");
  api.disconnect().done(function(){
    checkClosed3();
  }).fail(function(jqxhr, msg){
    ok( false, "disconnecting failed: "+msg );
  }).always(start);
});

asyncTest("second disconnect", function() {
  expect( 4 );
  api.disconnect().done(function(){
    ok(false, "disconnect claims to be successful")
  }).fail(function(jqxhr, err){
    equal( err, "not connected", "disconnect failed because not connected" );
    checkClosed3();
  }).always(start);
});