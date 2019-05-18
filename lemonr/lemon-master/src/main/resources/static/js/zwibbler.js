var zwibbler = Zwibbler.create("zwibbler1", {
  autoPickTool: true,
  showToolbar: false,
  defaultPaperSize: "letter",
  pageView: true,
  pageSelectorDiv: "#inner-pages",
  defaultFontSize: 50,
  defaultFont: "Bitter",
  multilineText: true,
  defaultLineWidth: 0
});


$("#font").hide();
$("#font-increase").hide();
$("#font-decrease").hide();
$("#shadow").hide();
$("#transparent").hide();
$("#alignment").hide();

// Handle image uploads to the server. When the upload begins, display
// a notification and update it.

$("#insert-image").click(function(e) {
  $("#fileinput").click();
});

$("#pick-tool").click(function(e) {
  zwibbler.usePickTool();
});

$("#text-tool").click(function(e) {
  zwibbler.useTextTool();
});

$("#undo").click(function(e) {
  zwibbler.undo();
});

$("#redo").click(function(e) {
  zwibbler.redo();
});

$("#cut").click(function(e) {
  zwibbler.copy();
  zwibbler.deleteSelection();
});

$("#copy").click(function(e) {
  zwibbler.copy();
});

$("#paste").click(function(e) {
  zwibbler.paste();
});

$("#fileinput").on("change", function(e) {
  var form = this.parentNode;
  upload(form);
  form.reset();
});

$("#add-page").click(function(e) {
  zwibbler.setCurrentPage(zwibbler.insertPage(zwibbler.getCurrentPage()+1));
});

$("#delete-page").click(function(e) {
  zwibbler.deletePage(zwibbler.getCurrentPage());
});

$("#square-tool").click(function(e) {
  zwibbler.useSquareTool();
});

$("#circle-tool").click(function(e) {
  zwibbler.useCircleTool();
});

$("#pencil-tool").click(function(e) {
  zwibbler.useBrushTool();
});

var Zoom = "page";
zwibbler.setZoom(Zoom);

$("#zoom").click(function(e) {
  if (Zoom === "page") {
    Zoom = "width";
  } else {
    Zoom = "page";
  }
  zwibbler.setZoom(Zoom);
});


var Fonts = [
  "Special Elite",
  "Bitter",
  "Love Ya Like A Sister"
];

var Properties = {};

// When the selection changes, zwibbler will call this function
zwibbler.on("selected-nodes", function(e) {
  var ids = zwibbler.getSelectedNodes();

  var types = {};
  Properties = {
    "shadow": false,
    "font": "Bitter",
    "fillStyle": "#808080",
    "textAlign": "left",
    "fontSize": 20
  };

  for(var i = 0; i < ids.length; i++ ) {
    var type = zwibbler.getNodeType(ids[i]);
    if (type === "PathNode") {
      types[type] = true;
      if (zwibbler.getNodeProperty(ids[i], "closed")) {
        type = "PathNode-closed";
      } else {
        type = "PathNode-open";
      }
    } else if (type === "TextNode") {
      Properties["fontName"] = zwibbler.getNodeProperty(ids[i],
                                                        "fontName");
    }

    var shadow = zwibbler.getNodeProperty(ids[i], "shadow");
    if (shadow !== undefined) {
      Properties["shadow"] = shadow;
    }

    var fillStyle = zwibbler.getNodeProperty(ids[i], "fillStyle");
    if (fillStyle !== undefined) {
      Properties["fillStyle"] = fillStyle;
    }

    var alignment = zwibbler.getNodeProperty(ids[i], "textAlign");
    if (alignment !== undefined) {
      Properties["textAlign"] = alignment;
    }

    var fontSize = zwibbler.getNodeProperty(ids[i], "fontSize");
    if (fontSize !== undefined) {
      Properties["fontSize"] = fontSize;
    }

    types[type] = true;
  }

  $("#shadow").toggle(ids.length > 0);
  $("#font").toggle(types["TextNode"] === true);
  $("#transparent").toggle(types["PathNode-closed"] === true);
  $("#alignment").toggle(types["TextNode"] === true);
  $("#font-increase").toggle(types["TextNode"] === true);
  $("#font-decrease").toggle(types["TextNode"] === true);
});

$("#shadow").on("click", function(e) {
  Properties["shadow"] = !Properties["shadow"];
  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(), "shadow",
                           Properties["shadow"]);
});

$("#font").on("click", function(e) {
  for(var i = 0; i < Fonts.length; i++ ) {
    if ( Properties["fontName"] === Fonts[i] ) {
      i += 1;
      break;
    }
  }

  var newFont = Fonts[i%Fonts.length];
  Properties["fontName"] = newFont;

  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(), "fontName",
                           newFont);
});

$("#transparent").on("click", function(e) {
  var colour = Zwibbler.parseColour(Properties["fillStyle"]);
  if (colour.a === 1) {
    colour.a = 0.7;
  } else {
    colour.a = 1;
  }

  Properties["fillStyle"] = Zwibbler.makeColour(colour);

  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(),
                           "fillStyle", Properties["fillStyle"]);
});

$("#font-increase").click(function(e) {
  Properties["fontSize"] *= 1.1;
  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(),
                           "fontSize", Properties["fontSize"]);
});

$("#font-decrease").click(function(e) {
  Properties["fontSize"] *= 1/1.1;
  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(),
                           "fontSize", Properties["fontSize"]);
});

$("#alignment").click(function(e) {
  if (Properties["textAlign"] === "left") {
    Properties["textAlign"] = "centre";
  } else if (Properties["textAlign"] === "centre") {
    Properties["textAlign"] = "right";
  } else {
    Properties["textAlign"] = "left";
  }

  zwibbler.setNodeProperty(zwibbler.getSelectedNodes(), "textAlign",
                           Properties["textAlign"]);
});

// When the upload is done either successfully or due to an error, this
// function is called. In the successful case, the status is set to
// "ok". Otherwise it is set to an error message. The result contains
// the JSON decoded response from the server. 
// 
// This function must then insert the image into the document.
function uploadDone(status, result) {
  if (status === "ok") {
    // *******************************************************
    // *******************************************************
    // *******************************************************
    // CHANGE THIS CODE TO PROCESS YOUR OWN RESPONSE
    // *******************************************************
    // *******************************************************
    // *******************************************************
    var url = "http://zwibbler.com/temp.py?fileid=" +
        result.fileid;

    zwibbler.beginTransaction();
    var nodeId = zwibbler.createNode("ImageNode", {
      url: url
    });
    zwibbler.translateNode(nodeId, 100, 100);
    zwibbler.commitTransaction();
  }
}

function upload( form )
{
  var progress = new ProgressNotification("Reading file");
  var xhr = new XMLHttpRequest();
  var fd = new FormData(form);

  xhr.upload.addEventListener("progress", 
                              function( e ) {
    progress.update( e.loaded / e.total );
  }, false
                             );

  xhr.addEventListener("load", 
                       function( e ) {
    progress.done();
    uploadDone( "ok", $.parseJSON( xhr.response ) );
  }, false
                      );

  xhr.addEventListener("error", 
                       function( e ) {
    progress.error("Error");
    uploadDone( "error", null );
  }, false
                      );

  xhr.addEventListener("abort", 
                       function( e ) {
    progress.error("Aborted");
    uploadDone( "aborted", null );
  }, false
                      );

  xhr.open(form.method, form.action);
  xhr.send(fd);
}

// Display multiple upload progress notifications
function ProgressNotification(name)
{
  this.name = name;
  ProgressNotification.all.push(this);
  this.div = $("<div>");
  $("#progress").append(this.div).show();
  this.update(0);
}

ProgressNotification.all = [];
ProgressNotification.prototype = {
  update: function(percent) {
    this.div.text(this.name + "... " + Math.round(percent * 100) +
                  "%");
  },

  error: function(message) {
    var self = this;
    var input = $("<input>").
    attr("type", "button").
    val("OK");

    input.click(function(e) {
      self.done();
    });

    this.div.html(this.name + "... " +  message);
    this.div.append(input);
  },

  done: function() {
    this.div.remove();
    var all = ProgressNotification.all;
    for(var i = 0; i < all.length; i++) {
      if (all[i] === this) {
        all.splice(i, 1);
        break;
      }
    }

    if (all.length === 0) {
      $("#progress").hide();
    }
  }
};

var lawrence = null;
// ----------------------------------------------------------------
// EXAMPLE CODE FOR SAVING THE DOCUMENT.
function SaveDocument() {
  $.ajax({
    url: "http://zwibbler.com/temp.py",
    type: "POST",
    data: {file: zwibbler.save()},

    success: function(response, status, xhr) {
      $("#working").hide();
      location.hash = "fileid=" + response.fileid;
    },

    error: function(response, status, xhr) {
      $("#working").hide();
      alert("Error: " + status);
    }
  });
  $("#working").show();
  var saved = null;
  saved = zwibbler.save("zwibbler3");
  lawrence = saved;
  console.log(saved);
  console.log('fuck');
  var dataUrl = zwibbler.save("png");
  window.open(dataUrl, "other");
}



// ----------------------------------------------------------------
// EXAMPLE CODE FOR OPENING THE DOCUMENT
function OpenDocument(fileid) {
  $.ajax({
    url: "http://zwibbler.com/temp.py",
    type: "GET",
    data: { fileid: fileid },

    success: function(response, status, xhr) {
      $("#working").hide();
      zwibbler.load(response);
    },

    error: function(response, status, xhr) {
      $("#working").hide();
      alert("Error: " + status);
    }
  });
  $("#working").show();
}

$("#save-document").click(function(e) {
  SaveDocument();
});

$("#new-document").click(function(e) {
  if (!zwibbler.dirty() || confirm("Discard changes?")) {
    zwibbler.newDocument();
    location.hash = "";
  }
});

$("#load-document").click(function(e) {
  var omg = 'zwibbler3.[{"type":"document","width":816,"height":1056},{"id":0,"type":"GroupNode","fillStyle":"#cccccc","strokeStyle":"#000000","lineWidth":2,"shadow":false,"matrix":[1,0,0,1,0,0],"layer":"default"},{"id":1,"type":"PageNode","parent":0,"fillStyle":"#cccccc","strokeStyle":"#000000","lineWidth":2,"shadow":false,"matrix":[1,0,0,1,0,0],"layer":"default"},{"id":2,"type":"PathNode","parent":1,"fillStyle":"#e0e0e0","strokeStyle":"#000000","lineWidth":0,"shadow":false,"matrix":[0.20174999999999954,0,0,2.533083333333333,592.9375,426.00416666666666],"layer":"default","textFillStyle":"#000000","fontName":"Arial","fontSize":50,"dashes":"","shapeWidth":0,"smoothness":0.3,"sloppiness":0,"closed":true,"arrowSize":0,"arrowStyle":"simple","doubleArrow":false,"text":"","roundRadius":0,"wrap":true,"angleArcs":0,"commands":[0,-50,-50,1,50,-50,1,50,50,1,-50,50,1,-50,-50,7],"seed":65111},{"id":3,"type":"PathNode","parent":1,"fillStyle":"#e0e0e0","strokeStyle":"#000000","lineWidth":0,"shadow":false,"matrix":[0.7173333333333329,0,0,1.5691666666666668,441.625,182.78333333333333],"layer":"default","textFillStyle":"#000000","fontName":"Arial","fontSize":50,"dashes":"","shapeWidth":0,"smoothness":0.3,"sloppiness":0,"closed":true,"arrowSize":0,"arrowStyle":"simple","doubleArrow":false,"text":"","roundRadius":0,"wrap":true,"angleArcs":0,"commands":[0,-50,-50,1,50,-50,1,50,50,1,-50,50,1,-50,-50,7],"seed":39806},{"id":4,"type":"PathNode","parent":1,"fillStyle":"#e0e0e0","strokeStyle":"#000000","lineWidth":0,"shadow":false,"matrix":[2.9814166666666666,0,0,1.7933333333333337,75.11249999999997,382.2916666666667],"layer":"default","textFillStyle":"#000000","fontName":"Arial","fontSize":50,"dashes":"","shapeWidth":0,"smoothness":0.3,"sloppiness":0,"closed":true,"arrowSize":0,"arrowStyle":"simple","doubleArrow":false,"text":"","roundRadius":0,"wrap":true,"angleArcs":0,"commands":[0,-50,-50,1,50,-50,1,50,50,1,-50,50,1,-50,-50,7],"seed":10996},{"id":5,"type":"ImageNode","parent":1,"fillStyle":"#cccccc","strokeStyle":"#000000","lineWidth":2,"shadow":false,"matrix":[1,0,0,1,48.441666666666634,50.68333333333334],"layer":"default","url":"http://zwibbler.com/temp.py?fileid=QvOvbhzjVMMrGYf00LhtXKzCQSg"},{"id":6,"type":"ImageNode","parent":1,"fillStyle":"#cccccc","strokeStyle":"#000000","lineWidth":2,"shadow":false,"matrix":[1,0,0,1,84.30833333333334,586.4416666666666],"layer":"default","url":"http://zwibbler.com/temp.py?fileid=Th7ns0qdpu2lgdNvLEcEtKidMPk"}]';

  zwibbler.load("zwibbler3", omg);
});


// ---------------------------------------------------------------
// EXAMPLE CODE
// When the page is loaded, and it contains a fileid= parameter,
// then open that document.
function SplitQueryString(string, separator) {
  var a, field, fields, i, index, key, value, _i, _len;
  separator = separator || "?";
  a = {};
  index = string.indexOf(separator);
  if (index >= 0) {
    string = string.substr(index + 1);
  }
  index = string.indexOf('#');
  if (index >= 0) {
    string = string.substr(0, index);
  }
  fields = string.split("&");
  for (_i = 0, _len = fields.length; _i < _len; _i++) {
    field = fields[_i];
    i = field.split("=");
    key = decodeURIComponent(i[0]);
    value = i.length > 1 ? decodeURIComponent(i[1]) : "";
    if (key.length) {
      a[key] = value;
    }
  }
  return a;
}

var hash = SplitQueryString(location.hash, "#");
if ("fileid" in hash) {
  OpenDocument(hash["fileid"]);
}