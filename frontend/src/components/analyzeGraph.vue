<template>
    <svg viewBox="0 0 350 215" preserveAspectRatio="xMinYMin meet" :id="graphId"/>
</template>

<script>
  import * as d3 from "d3";

  export default {
    name: "agraph",
    data: function () {
      return {
        graph: null,
        links: null,
        nodes: null,
        color: d3.scaleOrdinal(d3.schemeCategory20),
        simulation: null,
        settings: {
          strokeColor: "#29B5FF",
          svgWidth: 0,
          svgHeight: 0
        },
        edgepaths: null,
        edgelabels: null,
        linkedByIndex: {},
        selectNodes: {},
        isSelecting: false,
        selectNode: null,
        selectEdge: {}
      };
    },
    props: {
      graphData: Object,
      graphId: String,
      initGraph: Object
    },
    mounted: function () {
      var that = this;
      var svg = d3.select("#" + that.graphId);
      var bbox = svg.attr("viewBox").split(" ");
      this.settings.svgWidth = bbox[2] * 1;
      this.settings.svgHeight = bbox[3] * 1;
      this.graph = JSON.parse(JSON.stringify(this.graphData));

      // console.log("agraph");
      // console.log(this.graph);

      for (var i = 0; i < that.initGraph.nodes.length; i++) {
        that.selectNode = that.initGraph.nodes[i].id;
        that.selectNodes[that.initGraph.nodes[i].id] = true;
      }
      for (var i = 0; i < that.initGraph.links.length; i++) {
        var link = that.initGraph.links[i];
        that.selectNode = link.source;
        that.selectNodes[link.source] = true;
        that.selectNodes[link.target] = true;
        that.selectEdge[link.source + "," + link.target] = true;
        if (!(that.graphId.slice(-7) === "pattern")) {
          that.initGraph.nodes.push({'id': link.source});
          that.initGraph.nodes.push({'id': link.target});
        }
      }
      that.graph.links.forEach(function (d) {
        that.linkedByIndex[d.source + "," + d.target] = true;
        that.linkedByIndex[d.target + "," + d.source] = true;
      });

      function isConnected(a, b) {
        return that.linkedByIndex[a + "," + b];
      }

      that.simulation = d3
          .forceSimulation(that.graph.nodes)
          .force(
              "link",
              d3
                  .forceLink(that.graph.links)
                  .id(function (d) {
                    return d.id;
                  })
                  .distance(200)
          )
          .force("charge", d3.forceManyBody())
          .force(
              "center",
              d3.forceCenter(that.settings.svgWidth / 2, that.settings.svgHeight / 2)
          );
      d3.select("#" + that.graphId)
          .append("defs")
          .append("marker")
          .attr("id", "arrowhead" + that.graphId)
          .attr("viewBox", "0 -15 30 30")
          .attr("refX", 25)
          .attr("refY", 0)
          .attr("orient", "auto")
          .attr("markerWidth", 7)
          .attr("markerHeight", 7)
          .attr("xoverflow", "visible")
          .append("svg:path")
          .attr("d", "M 0,-10 L 10 ,0 L 0,10")
          .attr("fill", "#999")
          .style("stroke", "none");

      that.edgepaths = d3
          .select("#" + that.graphId)
          .selectAll(".edgepath")
          .data(that.graph.links)
          .enter()
          .append("path")
          .attr("class", "edgepath")
          .attr("fill-opacity", 0)
          .attr("stroke-opacity", 0)
          .style("pointer-events", "none")
          .attr("id", function (d, i) {
            return that.graphId + "edgepath" + i;
          });

      that.edgelabels = d3
          .select("#" + that.graphId)
          .selectAll(".edgelabel")
          .data(that.graph.links)
          .enter()
          .append("text")
          .style("pointer-events", "none")
          .attr("class", "edgelabel")
          .attr("id", function (d, i) {
            return that.graphId + "edgelabel" + i;
          })
          .attr("font-size", 0)
          .attr("fill", "#000");

      that.edgelabels
          .append("textPath")
          .attr("xlink:href", function (d, i) {
            return "#" + that.graphId + "edgepath" + i;
          })
          .style("text-anchor", "middle")
          .style("pointer-events", "none")
          .style("opacity", "1.0")
          .attr("startOffset", "50%")
          .text(function (d) {
            return d.label;
          });

      that.edgelabels.attr("font-size", function (d) {
        if (that.selectEdge.hasOwnProperty(d.source.id + "," + d.target.id)) {
          return 18;
        } else {
          return 0;
        }
      });

      that.links = d3
          .select("#" + that.graphId)
          .append("g")
          .attr("class", "alinks")
          .selectAll("line")
          .data(that.graph.links)
          .enter()
          .append("path")
          .attr("stroke-width", function (d) {
            if (that.selectEdge.hasOwnProperty(d.source.id + "," + d.target.id)) {
              return "4";
            } else {
              return "0";
            }
          })
          .attr("marker-end", "url(#arrowhead" + that.graphId + ")");

      that.nodes = d3
          .select("#" + that.graphId)
          .selectAll("node")
          .data(that.graph.nodes)
          .enter()
          .append("g")
          .attr("class", "anodes")
          .on("click", function (d) {
            if (d._clickid) {
              clearTimeout(d._clickid);
              d._clickid = null;
              // double click event
              if (that.isSelecting) {
                that.isSelecting = false;
                that.nodes.style("opacity", function (o) {
                  if (that.selectNodes[o.id]) {
                    return "1.0";
                  } else {
                    return "0.0";
                  }
                });
                that.edgelabels

                    .style("opacity", function (o) {
                      if (that.selectEdge[o.source.id + "," + o.target.id]) {
                        return "1.0";
                      } else {
                        return "0.0";
                      }
                    })
                    .style("fill", function (o) {
                      if (that.selectEdge[o.source.id + "," + o.target.id]) {
                        return "#000";
                      }
                    });
                that.links.attr("stroke-width", function (o) {
                  if (that.selectEdge[o.source.id + "," + o.target.id]) {
                    // if(that.selectNodes[o.source.id] && that.selectNodes[o.target.id]) {
                    return 4;
                  } else {
                    return 0;
                  }
                });
              } else {
              }
            } else {
              d._clickid = setTimeout(
                  function () {
                    // click event
                    if (that.isSelecting) {
                      if (isConnected(d.id, that.selectNode)) {
                        that.isSelecting = false;
                        that.selectNodes[d.id] = true;
                        that.initGraph.nodes.push({'id': d.id});
                        that.selectEdge[d.id + "," + that.selectNode] = true;
                        // that.initGraph.links
                        for (var i = 0; i < that.graph.links.length; i++) {
                          if ((that.graph.links[i].source.id == that.selectNode && that.graph.links[i].target.id == d.id) ||
                              (that.graph.links[i].source.id == d.id && that.graph.links[i].target.id == that.selectNode)) {
                            that.initGraph.links.push({
                              "source": that.graph.links[i].source.id,
                              "target": that.graph.links[i].target.id,
                              "id": that.graph.links[i].id
                            });
                          }
                        }
                        // console.log(that.initGraph)
                        that.selectEdge[that.selectNode + "," + d.id] = true;
                        that.nodes.style("opacity", function (o) {
                          if (that.selectNodes[o.id]) {
                            return "1.0";
                          } else {
                            return "0.0";
                          }
                        });
                        that.edgelabels
                            .attr("font-size", function (o) {
                              if (that.selectEdge[o.source.id + "," + o.target.id]) {
                                return 18;
                              } else {
                                return 0;
                              }
                            })
                            .style("fill", function (o) {
                              if (that.selectEdge[o.source.id + "," + o.target.id]) {
                                return "#000";
                              }
                            });
                        that.links.attr("stroke-width", function (o) {
                          if (that.selectEdge[o.source.id + "," + o.target.id]) {
                            // if(that.selectNodes[o.source.id] && that.selectNodes[o.target.id]) {
                            return 4;
                          } else {
                            return 0;
                          }
                        });
                      }
                    } else if (that.selectNodes[d.id]) {
                      that.isSelecting = true;
                      that.selectNode = d.id;
                      that.nodes.style("opacity", function (o) {
                        if (that.selectNodes[o.id]) {
                          return "1.0";
                        } else {
                          if (isConnected(o.id, d.id)) {
                            return "0.3";
                          }
                        }
                        return "0.0";
                      });
                      that.edgelabels
                          .attr("font-size", function (o) {
                            if (that.selectEdge[o.source.id + "," + o.target.id]) {
                              return 18;
                            } else if (o.source.id === d.id || o.target.id === d.id) {
                              return 18;
                            } else {
                              return 0;
                            }
                          })
                          .style("fill", function (o) {
                            if (that.selectEdge[o.source.id + "," + o.target.id]) {
                              return "#000";
                            } else if (o.source.id === d.id || o.target.id === d.id) {
                              return "lightgrey";
                            }
                          });

                      that.links.attr("stroke-width", function (o) {
                        if (that.selectEdge[o.source.id + "," + o.target.id]) {
                          return 4;
                        } else if (o.source.id === d.id || o.target.id === d.id) {
                          // if(isConnected(o.source.id, d.id) || isConnected(o.target.id, d.id)) {
                          // if(that.selectNode[o.source.id] || that.selectNode[o.target.id]) {
                          return 4;
                        } else {
                          return 0;
                        }
                      });
                    }
                    d._clickid = null;
                  }.bind(this),
                  350
              );
            }
          })
          .call(
              d3
                  .drag()
                  .on("start", function (d) {
                    if (!d3.event.active) that.simulation.alphaTarget(0.3).restart();
                    d.fx = d.x;
                    d.fy = d.y;
                  })
                  .on("drag", function dragged(d) {
                    d.fx = d3.event.x;
                    d.fy = d3.event.y;
                  })
          );

      for (var j = 0; j < 5; j++) that.color(j);
      that.nodes
          .append("circle")
          .attr("r", 15)
          .style("fill", function (d, i) {
            if (that.graphId.slice('-7') === 'pattern') return that.color(d.typeId);
            else return that.color(d.id);
          })
          .style("opacity", "1.0");
      that.nodes
          .append("text")
          .attr("font-size", 18)
          .attr("dx", function(d) {
            return -5 * d.type.length;
          })
          .attr("dy", -20)

          .text(function (d) {
            return d.type;
          });
      that.nodes.style("opacity", function (d, i) {
        if (that.selectNodes[d.id]) return "1.0";
        else return "0.0";
      });

      that.simulation.nodes(that.graph.nodes).on("tick", function ticked() {
        var radius = 15;
        that.nodes.attr("transform", function (d) {
          d.x = Math.max(radius, Math.min(that.settings.svgWidth - radius, d.x));
          d.y = Math.max(radius, Math.min(that.settings.svgHeight - radius, d.y));
          return "translate(" + d.x + ", " + d.y + ")";
        });

        that.links.attr("d", linkPath);
        that.edgepaths.attr("d", linkPath);
        that.edgelabels.attr("transform", function (d) {
          if (d.target.x < d.source.x) {
            var bbox = this.getBBox();
            var rx = bbox.x + bbox.width / 2;
            var ry = bbox.y + bbox.height / 2;
            return "rotate(180 " + rx + " " + ry + ")";
          } else {
            return "rotate(0)";
          }
        });
        //   }
      });
    }
  };

  function linkPath(d) {
    let x1 = d.source.x,
        y1 = d.source.y,
        x2 = d.target.x,
        y2 = d.target.y,
        dx = x2 - x1,
        dy = y2 - y1,
        dr = Math.sqrt(dx * dx + dy * dy),
        // Defaults for normal edge.
        drx = dr,
        dry = dr,
        xRotation = (180 / Math.PI) * Math.atan(dy / dx), // degrees
        largeArc = 0, // 1 or 0
        sweep = 1; // 1 or 0
    // Self edge.
    if (x1 === x2 && y1 === y2) {
      // Fiddle with this angle to get loop oriented.
      xRotation = -45;

      // Needs to be 1.
      largeArc = 1;

      // Change sweep to change orientation of loop.
      //sweep = 0;

      // Make drx and dry different to get an ellipse
      // instead of a circle.
      drx = 30;
      dry = 20;

      // For whatever reason the arc collapses to a point if the beginning
      // and ending points of the arc are the same, so kludge it.
      x2 = x2 + 1;
      y2 = y2 + 1;

      return (
          "M " +
          x1 +
          " " +
          y1 +
          " A " +
          drx +
          " " +
          dry +
          " " +
          xRotation +
          " " +
          largeArc +
          " " +
          sweep +
          " " +
          x2 +
          " " +
          y2
      );
    }

    return "M " + x1 + " " + y1 + " L " + x2 + " " + y2;
  }
</script>
<style>
    .alinks path {
        stroke: #999;
        fill: none;
        stroke-opacity: 0.6;
    }

    .anodes circle {
        stroke: #fff;
        stroke-width: 1.5px;
    }

</style>
