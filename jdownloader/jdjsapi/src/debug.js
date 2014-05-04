var api;

$(function(){
  $("pre").click(function(){
    eval($(this).text());
    $(this).stop(true).animate({"opacity":0},50).animate({"opacity":1},500);
  });
});