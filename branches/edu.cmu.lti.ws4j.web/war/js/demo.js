function insert_sampleS() {
  $('#s_mode').click()
  $('#s1').val("Eventually, a huge cyclone hit the entrance of my house.");
  $('#s2').val("Finally, a massive hurricane attacked my home.");
}
function insert_sampleW() {
  $('#w_mode').click()
  $('#w1').val("dog#n#1");
  $('#w2').val("hunting_dog#n#1");
  $('#w1,#w2').each(validateWithWN);
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
  $('#s1,#s2').removeAttr('disabled');
  $('#w1,#w2').attr('disabled','true');
  $('#s1wrapper,#s2wrapper').slideDown();
  $('#w1wrapper,#w2wrapper').slideUp();
  $('.mode_label').text('Sentence');
}
function wordMode() {
  $('#w1,#w2').removeAttr('disabled');
  $('#s1,#s2').attr('disabled','true');
  $('#w1wrapper,#w2wrapper').slideDown();
  $('#s1wrapper,#s2wrapper').slideUp();
  $('.mode_label').text('Word');
}

/**
//for delay after keyup() etc
var delay = (function(){
  var timer = 0;
  return function(callback, ms){
    clearTimeout (timer);
    timer = setTimeout(callback, ms);
  };
})();
**/

var defcache = {};

function annotateDef() {
  $('.synset').each(function(){
    var o = $(this);
    var ew = o.offset().left > ($(document).scrollLeft() + $(window).width() / 2) ? 'e' : 'w';
    o.tipsy({
      gravity: 's'+ew,
      html: true,
      opacity: 1,
      title: function() {
        var wps = o.text();
        var title = defcache[wps];
        if (typeof title === 'undefined') {
          var request = $.ajax({ 
		type: "GET", 
		async: false,
		url:"/wn", 
		data: { mode:"def", q: wps }, 
		dataType: "html" } );
          request.done( function(msg) { 
            title = msg;
            defcache[wps] = title;
	  }).fail(function(obj,err1,err2) {
	    title = "ERROR: "+err2;
	  });
	}
	return title;
	}});
  });
}

function validateWithWN() {
  var w = $(this);
  var request = $.ajax({ 
    type: "GET", 
    url:"/wn", 
    data: { mode:"validate", q: w.val() }, 
    dataType: "html" } );
  request.done( function(msg) { 
      var info = w.parent().find('.info');
      info.html(msg);
      //info.fadeIn("slow");
      info.css({opacity: 0.0, visibility: "visible"}).animate({opacity: 1.0});
  });
}

function scrollTo( id ) {
  $("html, body").animate({ scrollTop: $('#'+id).offset().top }, 2000);
}

/** init **/
$(document).ready(function () {
    //default: word
    var wMode = $('input[name=mode]:checked').val()=='w';
    $('.mode_label').text( wMode ? 'Word' : 'Sentence');
    
    //w1/w2 wn-validation settings
    $('#w1,#w2').focusout( validateWithWN ).focusin(function() {
	//$(this).parent().find('.info').fadeOut("slow");//losing the height
	$(this).parent().find('.info').fadeTo(200,0);//not losing the height
	//zips away
	//$(this).parent().find('.info').css({opacity: 0.0, visibility: "hidden"}).animate({opacity: 1.0});
    });
});

/** wordnet warmup (load wn in memory on serverside) **/
$(document).ready(function () {
  var loaded = false;
  //initialize dictionary
  var jqxhr = $.ajax({ type: "GET", url:"/suggest", data: { term: "warm_up" } } );
  jqxhr.done(function() { 
    p.css('width','100%');
    p.parent().attr('class','progress progress-info');
    loaded = true;
  });
  //.fail(function() { alert("error"); })
  //.always(function() { alert("complete"); });
  
  setTimeout(function(){
    if (!loaded) $('#progress_container').fadeIn('slow');
  }, 500);
  
  var i=0;
  var estimated_sec = 35;
  var wait = 300;//msec
  var p = $('#progress');
  function progress() {
    var per = i * 100 / (estimated_sec * 1000 / wait);
    // Stop when progress bar 90% or above.
    if ( loaded || per>=90 ) {
      return;
    }
    p.css('width',per+'%');
    i++;
    setTimeout(progress, wait);
  }
  progress();
  
  annotateDef();
});

/** tipsy **/
$(document).ready(function () {
  $('#w1,#w2').tipsy({gravity: 'w', opacity: 1});
  $('.g').each(function(){//NG: .num 
    var o = $(this);
    //var title = o.attr('title');
    //if (typeof title !== 'undefined') {
      o.tipsy({gravity: 's', opacity: 1});
    //}
  });
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

//Google Analytics
$(document).ready(function () {
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
  ga('create', 'UA-163510-12', 'ws4jdemo.appspot.com');
  ga('send', 'pageview');
});

/** Start WS4J async loading **/
$(document).ready(function () {
  function runWS(m, i, batchSize) {
    var cells = $('#'+m+'_table .num');
    var size = cells.length;
    if (size==0) return;
    var args = '';
    for (var j=0; j<batchSize; j++) {
      var pair = $(cells[i+j]).attr('title');
      if (typeof pair === 'undefined') break;
      args += args.length>0 ? ',' : '';
      args += pair;
    }
    var request = $.ajax({ 
      type: "GET", 
      url:"/ws4j", 
      data: { 'measure':m, 'args': args, 'batch_id': i }, 
      dataType: "json" } );
    request.done( function(data) {//async!! pay attention to the scope of var e.g. i, m
      var batchId = data['batch_id']*1;
      var measure = data['measure'];//m out of scope
      var result = data['result'];
      for ( var j=0; j<result.length; j++ ) {
        var r = result[j];
        var score = r['score'];
        var w1 = r['input1'];
        var w2 = r['input2'];
        var o = $('#'+measure+(batchId+j));
        var url = "?w1="+w1.replace(/#/g,'%23')+"&w2="+w2.replace(/#/g,'%23')+"&measure="+measure+"&mode=w";
        o.html('<a href="'+url+'" target="_blank">'+score+'</a>');
        o.attr('title',measure+'( '+w1+' , '+w2+' ) = '+score);
        o.tipsy({gravity: 's', opacity: 1});
      }
      i += batchSize;
      if (i < size) {
        runWS(measure, i, batchSize);
      }
    });//end of done()
    request.fail(function(msg,s1,s2) { alert("error: "+s2); })
  }

  /* starting point */
  //measures and batch size (how many calcs to do per one API call).
  var measures = {'wup':15, 'res':15, 'jcn':15, 
      'lin':15, 'lch':15, 'path':15,
      'lesk':4, 'hso':2};
  for ( var m in measures ) {
    var batchSize = measures[m];
    runWS(m, 0, batchSize);
  }
});
