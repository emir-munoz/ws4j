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
  if ($.trim(t1).length==0||$.trim(t2).length==0) {
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

function annotateDef(synsets) {
  synsets.each(function(){
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
		dataType: "html",
		timeout: 3000} );
          request.done( function(msg) { 
            title = msg;
            defcache[wps] = title;
	  }).fail(function(obj,err1,err2) {
	    title = err2;
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
  $("html, body").animate({ scrollTop: $('#'+id).offset().top }, 1000);
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

/** 
  WordNet warm-up 
  Loads WordNet on server-side memory asynchronously.
**/
function warmup() {
  var loaded = false;
  //initialize dictionary
  var p = $('#progress');
  var jqxhr = $.ajax({ 
    type: "GET", 
    url:"/suggest", 
    data: { term: "warm_up" } } );
  jqxhr.done(function() { 
    p.css('width','100%');
    p.parent().attr('class','progress progress-info');
    loaded = true;
  });
  //.fail(function() { alert("error"); })
  //.always(function() { alert("complete"); });
  
  setTimeout(function(){
    if (loaded) return;
    var i=0;
    var estimated_sec = 35;
    var wait = 600;//msec
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
    $('#progress_container').fadeIn('slow');
  }, 500);
}

$(document).ready(function(){
  warmup();
  //Periodically resend warm-up request so that GAE instance doesn't get killed. 
  // -> turned off as front instance hour easily exceeds the quota. 
  //var intervalID = setInterval(warmup, 300*1000);//sec
});

/** tipsy **/
$(document).ready(function () {
  $('#w1,#w2').tipsy({gravity: 'w', opacity: 1});
  $('#progress_container_label').tipsy({gravity: 'sw', opacity: 1});
  $('.g').tipsy({gravity: 's', opacity: 1});
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
  function runSentence(measure, i, batchSize) {
    var cells = $('#'+measure+'_table .num');
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
      data: { 
        'measure':measure, 
        'args': args, 
        'batch_id': i }, 
      dataType: "json",
      timeout: 60000} );
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
        runSentence(measure, i, batchSize);
      }
    });//end of done()
    request.fail(function(msg,s1,s2) { console.log("error: "+s2); });
  }
  
  function runWord(measure) {
    var w1 = $('#w1').val();
    var w2 = $('#w2').val();
    if (w1.length==0 || w2.length==0) return;
    w1 = w1.replace(/,/g,'');
    w2 = w2.replace(/,/g,'');
    var args = w1+'::'+w2;
    var request = $.ajax({ 
          type: "GET", 
          url:"/ws4j", 
          data: { 'measure':measure, 'args': args, 'trace': 1 }, 
          dataType: "json",
          timeout: 60000} );
    request.done(function(data){
      var m = data['measure'];
      var result = data['result'];
      var msec = ' msec.';
      for ( var j=0; j<result.length; j++ ) {
        var r = result[j];
        var score = r['score'];
        var s1 = r['input1'];
        var s2 = r['input2'];
        var trace = r['trace'];
        var time = r['time'];
        var error = r['error'];
        var n1 = r['input1_num'];
        var n2 = r['input2_num'];
        var timeMsg = time.toFixed(1)+" msec";
        if (n1*n2>1) {
          timeMsg += " ("+n1+" x "+n2+" pairs of synsets).<br>"+(time/(n1*n2)).toFixed(1)+" msec/pair.";
        } else {
          timeMsg += "/pair";
        }
        timeMsg += "<br>"+(n1*n2*1000/time).toFixed(1)+" pairs/sec";
        if (score<0) {//error
          score = "-1 <span class=\"error_reason\">["+error+"]</span>";
        } else {//no error
          s1 = '<span class=\"synset\">'+s1+'</span>';
          s2 = '<span class=\"synset\">'+s2+'</span>';
        }
        var combo = $('#combo_info');
        if (combo.length>0 && combo.html().length==0 && n1*n2>1) combo.html("Each score above is the highest from "+
            n1+" x "+n2+" pairs of synsets.");
        var summary = $('#'+m+'_summary');
        summary.html('( '+s1+' , '+s2+' ) = '+score+'\n');
        annotateDef(summary.find('.synset'));
        var sp = trace.split(msec);
        var trace_summary = sp.length==1?trace:sp[0]+msec;
        var trace_more = sp.slice(1).join(msec);
        var traceHtml = sp.length<=2 ? trace_summary : (trace_summary+'<br><br><input type=\"button\" value=\" + more log \" onclick=\"more(\''+m+'\')\">'
           +'<div id=\"'+m+'_more\" style=\"display:none\">'+trace_more+'</div>');
        $('#'+m+'_trace').html(traceHtml);
        $('#'+m+'_time').html(timeMsg);
      }
      annotateDef($('#'+m+'_trace .synset'));
    });//end of done()
    request.fail(function(msg,s1,s2) { 
      $('#'+measure+'_trace,#'+measure+'_time').html(s2);
      $('#'+measure+'_summary').html('( '+w1+' , '+w2+' ) = '+s2+'\n');
    });
  }

  /* starting point */
  //measures and batch size (how many calcs to do per one API call).
  var measures = {'wup':15, 'res':15, 'jcn':15, 
      'lin':15, 'lch':15, 'path':15,
      'lesk':4, 'hso':2};
  for ( var measure in measures ) {
    var batchSize = measures[measure];
    runSentence(measure, 0, batchSize);
    runWord(measure);
  }
});

function more(m) {
  var more = $('#'+m+'_more');
  if (more.css('display')=='none') {
    $('#'+m+'_trace input').val(' - less log ');
    more.slideDown('slow');
  } else {
    $('#'+m+'_trace input').val(' + more log ');
    more.slideUp('slow');
  }
}