<template>
    <svg viewBox="0 0 400 400" preserveAspectRatio="xMinYMin meet" :id="graphId"/>
</template>

<script>
  import * as d3 from "d3";

  export default {
    name: "graph",
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
        edgelabels: null
      };
    },
    props: {
      gData: Object,
      graphId: String
    },
    mounted: function () {
      // console.log(this.graphData);
      var that = this;
      var svg = d3.select("#" + that.graphId);
      var bbox = svg.attr("viewBox").split(" ");
      this.settings.svgWidth = bbox[2] * 1;
      this.settings.svgHeight = bbox[3] * 1;
      this.graph = JSON.parse(JSON.stringify(this.gData));

      // var tooltip = d3.select("body")
      //   .append("div")
      //   .attr("class", "tooltip")
      //   .style("opacity", 0);
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
          .attr("font-size", 20)
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

      that.nodes
          .append("circle")
          .attr("r", 15)
          .style("fill", function (d, i) {
            for (var i = 0; i < 5; i++) that.color(i);
            return that.color(d.typeId);
          });
      that.nodes
          .append("text")
          .attr("font-size", 24)
          .attr("dx", function (d) {
            return -6 * d.label.length;
          })
          .attr("dy", -20)
          .text(function (d) {
            return d.label;
          });
      that.simulation.nodes(that.graph.nodes).on("tick", function ticked() {
        var radius = 15;
        that.nodes.attr("transform", function (d) {
          d.x = Math.max(radius, Math.min(that.settings.svgWidth - radius, d.x));
          d.y = Math.max(radius, Math.min(that.settings.svgHeight - radius, d.y));
          return "translate(" + d.x + ", " + d.y + ")";
        });

        that.links.attr("d", linkPath);

        that.edgepaths.attr("d", linkPath2);

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

  function linkPath2(d) {
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
    if (x1 > x2)
      return "M " + x1 + " " + (y1 + 10) + " L " + x2 + " " + (y2 + 10);
    else
      return "M " + x1 + " " + (y1 - 10) + " L " + x2 + " " + (y2 - 10);
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
</style>
