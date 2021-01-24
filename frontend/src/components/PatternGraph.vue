<template>
    <div>
        <el-dialog
                title="Chart"
                :visible.sync="dialogVisible"
                width="40%" top="50px">
            <svg viewBox="0 0 400 400" :id="'pie' + graphId"></svg>
            <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false">Cancel</el-button>
    <el-button type="primary" @click="resetPieChart()">Reset</el-button>
    <el-button type="primary" @click="saveChart">Save</el-button>
    </span>
        </el-dialog>
        <svg viewBox="0 0 400 400" preserveAspectRatio="xMinYMin meet" :id="graphId"/>
    </div>
</template>

<script>
  import * as d3 from "d3";

  export default {
    name: "patternGraph",
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
        dialogVisible: false,
        prevId: {},
        tempd: null,
        reset: true,
        labelData: null,
        names: []
      };
    },
    props: {
      gData: Object,
      graphId: String,
      totalData: Array,
      index: Number
    },
    mounted: function () {
      // console.log("gData");
      // console.log(this.gData);
      // console.log(this.graphData);
      var that = this;
      var svg = d3.select("#" + that.graphId);
      var bbox = svg.attr("viewBox").split(" ");
      this.settings.svgWidth = bbox[2] * 1;
      this.settings.svgHeight = bbox[3] * 1;
      this.graph = JSON.parse(JSON.stringify(this.gData));

      // console.log("gData");
      // console.log(this.graph);

      that.prevId[that.index] = [];

      that.simulation = d3
          .forceSimulation(that.graph.nodes)
          .force(
              "link",
              d3
                  .forceLink(that.graph.links)
                  .id(function (d) {
                    return d.id;
                  })
                  .distance(300)
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
            return "#edgepath" + i;
          })
          .attr("font-size", 24)
          .attr("fill", "#000");

      that.edgelabels
          .append("textPath")
          .attr("xlink:href", function (d, i) {
            return "#" + that.graphId + "edgepath" + i;
          })
          .style("text-anchor", "middle")
          .style("pointer-events", "none")
          .attr("startOffset", "50%")
          .text(function (d) {
            return d.label;
          });
      that.links = d3
          .select("#" + that.graphId)
          .append("g")
          .attr("class", "links")
          .selectAll("line")
          .data(that.graph.links)
          .enter()
          .append("path")
          .attr("stroke-width", 4)
          .attr("marker-end", "url(#arrowhead" + that.graphId + ")");

      that.nodes = d3
          .select("#" + that.graphId)
          .selectAll("node")
          .data(that.graph.nodes)
          .enter()
          .append("g")
          .attr("class", "node")
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
          )
          .on('click', that.drawPieChart);

      that.nodes
          .append("circle")
          .attr("r", 15)
          .style("fill", function (d, i) {
            for (var i = 0; i < 5; i++) that.color(i);
            return that.color(d.typeId);
          })
          .attr("id", function (d, i) {
            return i;
          })
      that.nodes
          .append("text")
          .attr("font-size", 24)
          .attr("dx", function(d) {
            return -6 * d.type.length;
          })
          .attr("dy", -20)
          .text(function (d) {
            return d.type;
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
      });
    },

    methods: {
      drawPieChart(d) {
        var that = this;
        this.dialogVisible = true;
        that.tempd = d;
        var labels = {};
        var labelList = [];
        var instances = this.totalData[this.index].patternInstance;

        for (var i = 1; i < instances.length; i++) {
          if (that.reset || instances[i].show) {
            var nodes = instances[i].nodes;
            for (var j = 0; j < nodes.length; j++) {
              if (d.id === nodes[j].id) {
                var label = nodes[j].label.split("/")[0];
                if (label in labels) {
                  labels[label] += 1;
                } else {
                  labels[label] = 1;
                }
              }
            }
          }
        }
        for (var key in labels) {
          if (labels.hasOwnProperty(key)) {
            labelList.push({'label': key, 'value': labels[key]});
          }
        }
        if (that.names.length > 0 && that.names[that.names.length - 1].flag === 0) {
          that.names.pop();
        }
        that.names.push({"value": d.type, "flag": 0});
        // console.log(labelList);

        setTimeout(() => {
          this.drawPie(d, labelList);
        }, 0);
      },
      resetPieChart() {
        var that = this;
        // console.log(that.tempd);
        this.dialogVisible = false;
        var instances = that.totalData[that.index].patternInstance;
        for (var i = 1; i < instances.length; i++) {
          instances[i].show = true;
        }
        d3.select("#" + that.graphId)
            .selectAll(".node")
            .select("text").text(function (d) {
          return d.type;
        });
        that.names = [];
        that.reset = true;
        that.$emit('changeGraphData', that.totalData);
      },
      drawPie(d, data) {
        var that = this;
        this.labelData = data;
        var svg = d3.select("#pie" + that.graphId);
        svg.selectAll("*").remove();
        var bbox = svg.attr("viewBox").split(" "),
            width = bbox[2] * 1,
            height = bbox[3] * 1,
            radius = Math.min(width, height) / 2,
            g = svg.append("g").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
        var tempid = d.id;
        var total = 0;
        for (var i = 0; i < data.length; i++) {
          total += data[i].value;
        }
        data.sort((a, b) => (a.value) - (b.value));
        var labelDict = {};
        for (var i = data.length - 1; i >= 0 && data.length - i <= 10; i--) {
          labelDict[data[i].label] = true;
        }

        var color = d3.scaleOrdinal(d3.schemeCategory20);
        var pie = d3.pie()
            .sort(null)
            .value(function (d) {
              return d.value;
            });
        var path = d3.arc()
            .outerRadius(radius - 10)
            .innerRadius(0);
        var label = d3.arc()
            .outerRadius(radius - 70)
            .innerRadius(radius - 70);

        var arc = g.selectAll(".arc")
            .data(pie(data))
            .enter().append("g")
            .attr("class", "arc");

        const tooltip = d3.select(".tooltip");

        arc.append("path")
            .attr("d", path)
            .attr("fill", function (d, i) {
              return color(i);
            })
            .on("click", function (d) {
              var instances = that.totalData[that.index].patternInstance;
              var id;
              var label = d.data.label;
              for (var i = 1; i < instances.length; i++) {
                if (that.reset || instances[i].show) {
                  var nodes = instances[i].nodes;
                  var flag = false;
                  for (var j = 0; j < nodes.length; j++) {
                    if (nodes[j].id === tempid) {
                      id = nodes[j].id;
                      if ("label" in nodes[j]) {
                        var lab = nodes[j].label.split("/")[0];
                        if (d.data.label === lab) {
                          flag = true;
                          break;
                        }
                      }
                    }
                  }
                  instances[i].show = flag;
                }
              }
              if (that.names.length > 0 && that.names[that.names.length - 1].flag === 0) {
                that.names.pop();
              }
              that.names.push({"value": label, "flag": 1});
              that.dialogVisible = false;
              that.reset = false;
              var texts = d3.select("#" + that.graphId)
                  .selectAll(".node")
                  .select("text");
              texts.text(function (d) {
                if (d.id === id) return label;
                else return d3.select(this).text();
              });
              texts.attr("dx", function(d) {
                return -6 * d3.select(this).text().length;
          });
              that.$emit('changeGraphData', that.totalData);
            })
            .on('mouseover.tooltip', function (d) {
              tooltip.transition()
                  .duration(300)
                  .style("opacity", .8);
              tooltip.html("<br>Label: " + d.data.label + "<p>Value: " + d.data.value + "</p>")
                  .style("left", (d3.event.pageX) + "px")
                  .style("top", (d3.event.pageY + 10) + "px");
            })
            .on("mouseout.tooltip", function () {
              tooltip.transition()
                  .duration(100)
                  .style("opacity", 0);
            })
            .on("mousemove", function () {
              tooltip.style("left", (d3.event.pageX) + "px")
                  .style("top", (d3.event.pageY + 10) + "px");
            });

        arc.append("text")
            .attr("transform", function (d) {
              var midAngle = d.endAngle < Math.PI ? d.startAngle / 2 + d.endAngle / 2 : d.startAngle / 2 + d.endAngle / 2 + Math.PI;
              return "translate(" + label.centroid(d)[0] + "," + label.centroid(d)[1] + ") rotate(-90) rotate(" + (midAngle * 180 / Math.PI) + ")";
            })
            .attr("dy", ".15em")
            .attr('text-anchor', 'middle')
            .style('font-size', '15px')
            .text(function (d) {
              if (d.data.label in labelDict)
                return d.data.label + ": " + (d.data.value / total * 100).toFixed(2) + "%";
              else return "";
            });
      },
      saveChart() {
        this.dialogVisible = false;
        this.$emit('saveChart', d3.select('#pie' + this.graphId), this.labelData, this.index + 1, this.names);
      }
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
    .links path {
        stroke: #999;
        fill: none;
        stroke-opacity: 0.6;
    }

    .nodes circle {
        stroke: #fff;
        stroke-width: 1.5px;
    }

    div.tooltip {
        position: absolute;
        background-color: white;
        max-width: 200px;
        height: auto;
        padding: 1px;
        border-style: solid;
        border-radius: 4px;
        border-width: 1px;
        box-shadow: 3px 3px 10px rgba(0, 0, 0, .5);
        pointer-events: none;
    }

    .arc text {
        font: 10px sans-serif;
        text-anchor: middle;
    }

    .arc path {
        stroke: #fff;
    }
</style>
