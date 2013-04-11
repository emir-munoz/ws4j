function insert_sampleS() {
  $('#s_mode').click()
  $('#s1').val("Eventually, a huge cyclone hit the entrance of my house.");
  $('#s2').val("Finally, a massive hurricane attacked my home.");
}
function insert_sampleW() {
  $('#w_mode').click()
  $('#w1').val("dog#n#1");
  $('#w2').val("hunting_dog#n#1");
}
function validate() {
  var wMode = $('input[name=mode]:checked').val()=='w';
  var t1 = $(wMode ? '#w1' : '#s1').val(); 
  var t2 = $(wMode ? '#w2' : '#s2').val();
  if (t1.trim().length==0||t2.trim().length==0) {
	alert("Please fill in both of the text boxes.");
	return false;
  }
  return t1.length * t2.length <= 10000 || confirm("Input texts are long so it may take some time. Is it ok?");
}
function sentMode() {
  $('#s1wrapper,#s2wrapper').slideDown();
  $('#w1wrapper,#w2wrapper').slideUp();
  $('.mode_label').text('Sentence');
}
function wordMode() {
  $('#w1wrapper,#w2wrapper').slideDown();
  $('#s1wrapper,#s2wrapper').slideUp();
  $('.mode_label').text('Word');
}

/** init **/
$(document).ready(function () {
    var wMode = $('input[name=mode]:checked').val()=='w';
    $('.mode_label').text( wMode ? 'Word' : 'Sentence');
});

/** tipsy **/
$(document).ready(function () {
  $('#w1,#w2').tipsy({gravity: 'w'});
});

/** jqueryui autocomplete **/
$(document).ready(function () {
  var cache = {};
  $("#w1,#w2").each(function(){
    $( this ).autocomplete({
      source: function (request, response) {
        var term = request.term;
        if ( term in cache ) {
          response( cache[ term ] );
          return;
        }
        $.getJSON("suggest", {term: request.term}, function( data, status, xhr ) {
          cache[ term ] = data;
          response( data );
        });
      },
      minLength: 3,
      /**
      focus: function( event, ui ) {
	        $( this ).val( ui.item.label );
	        return false;
	      },
	      **/
      select: function( event, ui ) {
	 var flag = ui.item.label.split("#").length==1;
	 var v = ui.item.label + (flag ? "#" : "");
	 $( this ).val( v );
	 if (flag){
	   //need to use settimeout.
	   var that = $(this);
	   setTimeout(function(){that.autocomplete('search',v);}, 100);
	 }
	 return false;
      }
    }).data( "ui-autocomplete" )._renderItem = function( ul, item ) {
      return $( "<li>" )
        .append( "<a><strong>" + item.label+"</strong>" + 
        	(item.desc ? "<br>&nbsp;&nbsp;" + item.desc : "" ) + "</a>" )
        .appendTo( ul );
    };
  });
  //Hmm, forcing to pop up autocomplete has side effect in selecting
  //$("#w1").mousedown(function(){ setTimeout(function(){$("#w1").autocomplete("search");},1000)});
  //$("#w2").mousedown(function(){$("#w2").autocomplete("search");});
});