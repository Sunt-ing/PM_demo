<template>
    <div class="app">
        <div class="wrapper">
            <db-header></db-header>
            <!-- side bar -->
            <el-col :span="4" class="menu">
                <el-select id="dataset_select" v-model="dataset_select" @change='changeDataSet' placeholder="">
                    <el-option
                            v-for="item in options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                    ></el-option>
                </el-select>
                <el-card v-loading="dataset_loading"
                        class="side_card"
                        shadow="never"
                        style="height: 45%; overflow: auto; margin-top: 20px;"
                >
                    <div slot="header" class="clearfix" style="height:30px; padding: 4px 7px;">
                        <span>Node</span>
                    </div>
                    <div v-for="(node, index) in realGraph.nodes" :key="'node' + index" class="text item">
                        <el-button class="sidebar_btn" round :id="'button'+node.id"
                                   @click="init_graph_by_node(node.id)">
                            <i class="el-icon-menu" :style="'float: left; color: ' + color(node.id)"/>
                            {{ node.type }}
                        </el-button>
                    </div>
                </el-card>
                <el-card v-loading="dataset_loading" class="side_card" shadow="never" style="height: 45%; overflow: auto;">
                    <div slot="header" class="clearfix" style="height:30px; padding: 4px 8px;">
                        <span>Relation</span>
                    </div>
                    <div v-for="(link, index) in realGraph.links" :key="'link' + index" class="text item">
                        <el-button class="sidebar_btn" round @click="init_graph_by_edge(link)">
                            <div style="margin-left: -15px;">
                                <i class="el-icon-menu" :style="'float: left; color: ' + color(link.source)"/>
                                <i class="el-icon-right" style="float: left;"/>
                                <i class="el-icon-menu" :style="'float: left; color: ' + color(link.target)"/>
                            </div>
                            {{ link.label }}
                        </el-button>
                    </div>
                </el-card>
            </el-col>
            <!-- side bar end -->
            <el-col :span="20" class="content">
                <div id="mainpage">
                    <el-row id="mainrow" :gutter="4">
                        <el-col :span="6" id="analyze_part">
                            <div class="analyze_div">
                                <el-card class="box-card" id="analyze_card" shadow="never">
                                    <div slot="header" class="clearfix" style="height: 50px; padding: 10px;">
                                        <span style="height: 50px">Pattern Analyze</span>
                                    </div>
                                    <div id="analyze_svg">
                                        <el-collapse v-model="activeList">
                                            <div v-for="(item, index) in em_cardlist" :key="'em' + index">
                                                <el-collapse-item v-if="item[2] != 0" title="Pattern"
                                                                  :name="item[0].toString()">
                                                    <el-card
                                                            v-if="item[2] === 1"
                                                            :id="'card' + index"
                                                            class="em-card"
                                                            shadow="always"
                                                            style="height: 260px; text-align: center;"
                                                    >
                                                        <agraph
                                                                v-bind:graph-data="realGraph"
                                                                v-bind:graph-id="'agraph' + item[0]"
                                                                v-bind:initGraph="item[1]"
                                                        ></agraph>
                                                        <el-button-group style="float: right; z-index: 2;">
                                                            <el-button size="mini" type="success" round
                                                                       icon="el-icon-check"
                                                                       @click="submitPattern(item[1])"></el-button>
                                                            <el-button
                                                                    size="mini"
                                                                    type="danger"
                                                                    round
                                                                    icon="el-icon-close"
                                                                    @click="deleteCard(index)"
                                                            ></el-button>
                                                        </el-button-group>
                                                    </el-card>
                                                    <el-card
                                                            v-if="item[2] === 3"
                                                            :id="'card' + index"
                                                            class="em-card"
                                                            shadow="always"
                                                            style="height: 280px; text-align: center;"
                                                    >
                                                        <agraph
                                                                v-bind:graph-data="patternData"
                                                                v-bind:graph-id="'agraph' + item[0] + 'pattern'"
                                                                v-bind:initGraph="item[1]"
                                                        ></agraph>
                                                        `
                                                        <el-button-group style="float: right; z-index: 2;">
                                                            <el-button size="mini" type="success" round
                                                                       icon="el-icon-check"
                                                                       @click="submitPattern(item[1])"></el-button>
                                                            <el-button
                                                                    size="mini"
                                                                    type="danger"
                                                                    round
                                                                    icon="el-icon-close"
                                                                    @click="deleteCard(index)"
                                                            ></el-button>
                                                        </el-button-group>
                                                    </el-card>
                                                </el-collapse-item>
                                            </div>
                                        </el-collapse>
                                    </div>
                                </el-card>
                            </div>
                        </el-col>
                        <el-col :span="18" id="result_part" v-loading="loading">
                            <div class="graph_container">
                                <el-collapse v-if="graphData !== null" v-model="activeNames">
                                    <el-collapse-item :name="k" v-for="(k, index) in graphData.length"
                                                      :title="'No.' + k + ' Pattern，Instance: ' + (graphData[k-1].patternInstance.length-1) + ', Minimum Image Support: ' + graphData[k-1].frequency"
                                                      :key="index">
                                        <div>
                                            <el-row>
                                                <el-col v-if="activeNames.includes(k)" :span="8"
                                                        v-bind:key="'col' + k + '0'" ref="infobox">
                                                    <el-card v-if="graphData[k-1].patternInstance[0].show"
                                                             class="graph-card">
                                                        <div slot="header" class="clearfix" style="font-size: 16px">
                                                            <span>Pattern</span>
                                                            <el-checkbox v-model="showIns[k]"
                                                                         style="padding: -1px 20px">Instance
                                                            </el-checkbox>
                                                            <el-button
                                                                    style="float: right; padding: 3px 0"
                                                                    type="text"
                                                                    @click="init_graph_by_pattern(k-1)"
                                                            >Analyze
                                                            </el-button>
                                                        </div>
                                                        <pattern-graph
                                                                @changeGraphData="changeGraph"
                                                                @saveChart="saveChart"
                                                                v-bind:g-data="graphData[k-1].patternInstance[0]"
                                                                v-bind:totalData="graphData"
                                                                v-bind:index="k-1"
                                                                v-bind:graph-id="'graph' + '-' + k + '-' + '1'">
                                                        </pattern-graph>
                                                    </el-card>
                                                </el-col>
                                                <el-col :span="8"
                                                        v-for="(pie, index) in pieChart[k]" :key="index">
                                                    <el-card class="chart-header" v-if="pieChartShow[k][pie]">
                                                        <div slot="header" class="clearfix">
                                                          <span style="font-size: 16px;">
                                                            {{pieChartNames[k][pie-1]}}
                                                          </span>
                                                            <el-button
                                                                    style="float: right; padding: 3px 0"
                                                                    type="text"
                                                                    @click="delete_pieChart(k, pie)"
                                                            >Delete
                                                            </el-button>
                                                        </div>
                                                        <div :id="'chart' + '-' + k + '-' + pie"></div>
                                                    </el-card>
                                                </el-col>
                                                <el-col v-show="showIns[k]" :span="8"
                                                        v-for="i in graphData[k-1].patternInstance.length"
                                                        v-bind:key="'col' + k + i" ref="infobox">
                                                    <el-card v-if="i > 1 && graphData[k-1].patternInstance[i-1].show && activeNames.includes(k)"
                                                             class="graph-card">
                                                        <div slot="header" class="clearfix">
                                                          <span style="font-size: 16px">
                                                            {{i == 1 ? "Pattern": "Instance" + "-" + (i - 1)}}
                                                          </span>
                                                            <el-button
                                                                    style="float: right; padding: 3px 0"
                                                                    type="text"
                                                                    @click="delete_instance(k-1, i-1)"
                                                            >Delete
                                                            </el-button>
                                                        </div>
                                                        <graph
                                                                v-bind:g-data="graphData[k-1].patternInstance[i - 1]"
                                                                v-bind:graph-id="'graph' + '-' + k + '-' + i"
                                                        ></graph>
                                                    </el-card>
                                                </el-col>
                                            </el-row>
                                        </div>
                                    </el-collapse-item>
                                </el-collapse>
                            </div>
                        </el-col>
                    </el-row>
                </div>
            </el-col>
        </div>
        <div class="footer"></div>
    </div>
</template>

<script>
  import * as d3 from "d3";
  import Graph from "./components/Graph.vue";
  import agraph from "./components/analyzeGraph.vue";
  import PatternGraph from "./components/PatternGraph";
  import DbHeader from "./components/DbHeader.vue";
  // import Mainpage from './components/Mainpage.vue'
  import ElRow from "element-ui/packages/row/src/row";

  export default {
    name: "app",
    components: {
      ElRow,
      DbHeader,
      Graph,
      agraph,
      PatternGraph
    },
    data() {
      return {
        activeList: [],
        leftActiveName: "0",
        activeNames: [],
        card_cnt: 0,
        realGraph: {
          nodes: [],
          links: []
        },
        realData: {
          nodes: [
            {
              label: "State",
              name: "行政区",
              id: 0
            },
            {
              label: "C17",
              name: "分支",
              id: 1
            },
            {
              label: "C18",
              name: "毒株",
              id: 2
            },
            {
              label: "Country",
              name: "国家",
              id: 3
            },
            {
              label: "C16",
              name: "宿主",
              id: 4
            }
          ],
          links: [
            {
              source: 2,
              target: 0,
              id: 0,
              label: "from_country",
              type: "来自于"
            },
            {
              source: 4,
              target: 2,
              label: "host_is",
              type: "宿主是"
            },
            {
              source: 2,
              target: 1,
              label: "from_mutate_branch",
              type: "变异于"
            }
          ]
        },
        patternData: null,
        options: [
          {
            value: "1",
            label: "COVID-19 Research v1.1"
          },
          {
            value: "2",
            label: "COVID-19 Information v1.0"
          },
          {
            value: "3",
            label: "Taxonomy"
          }
        ],
        dataset_select: "1",
        color_arr: d3.scaleOrdinal(d3.schemeCategory20),
        em_cardlist: [],
        graphData: null,
        loading: false,
        pieChart: [],
        pieChartShow: [],
        showIns: [],
        dataset_loading: true,
        pieChartNames: []
      };
    },
    mounted: function () {
      for (var i = 0; i < 11; i++) {
        this.showIns[i] = false;
        this.pieChart[i] = 0;
        this.pieChartShow[i] = [true];
        this.pieChartNames[i] = [];
      }
      this.$axios.get('/sidebar', {
        params: {
          dataset: this.dataset_select
        }
      }).then(Response => {
            this.realGraph = Response.data;
            this.dataset_loading = false;
          });
      d3.select("body")
          .append("div")
          .attr("class", "tooltip")
          .style("opacity", 0)
          .style('z-index', '10000')
    },
    methods: {
      changeGraph(data) {
        this.graphData = [];
        this.graphData = data;
      },
      color(idx) {
        return this.color_arr(idx);
      },
      init_graph_by_node(idx) {
        this.card_cnt += 1;
        this.activeList.push(this.card_cnt.toString());
        var showGraph = {
          nodes: [
            {
              id: idx
            }
          ],
          links: []
        };
        var temp = this.card_cnt;
        // console.log(showGraph);
        this.em_cardlist.push([temp, showGraph, 1]);
      },
      init_graph_by_edge(link) {
        this.card_cnt += 1;
        this.activeList.push(this.card_cnt.toString());
        var temp = this.card_cnt;
        var showGraph = {
          nodes: [],
          links: [
            link
          ]
        };
        this.em_cardlist.push([temp, showGraph, 1]);
      },
      init_graph_by_pattern(idx) {
        this.card_cnt += 1;
        var temp = this.card_cnt;
        this.activeList.push(this.card_cnt.toString());
        // console.log('graphdata');
        // console.log(this.graphData[idx].patternInstance[0]);
        this.patternData = this.graphData[idx].patternInstance[0];
        // console.log("pattern Data");
        // console.log(this.patternData);
        this.em_cardlist.push([temp, this.patternData, 3]);
      },
      deleteCard(idx) {
        this.em_cardlist[idx][2] = 0;
        this.card_cnt += 1;
        var temp = this.card_cnt;
        this.em_cardlist.push([temp, idx, 0]);
        this.graphData = null;
      },
      submitPattern(graph) {
        console.log(graph);
        this.loading = true;
        var that = this;
        // console.log(JSON.stringify(graph));
        // console.log(this.dataset_select);
        this.$axios.get('/query', {
          params: {
            graph: JSON.stringify(graph),
            dataset: this.dataset_select
          }
        }).then(response => {
          that.graphData = null;
          that.graphData = response.data;
          for (var i = 0; i < 11; i++) {
            this.pieChart[i] = 0;
            this.showIns[i] = false;
            this.pieChartShow[i] = [true];
          }
          this.activeNames = [];
          for (var i = 0; i < that.graphData.length; i++) {
            for (var j = 0; j < that.graphData[i].patternInstance.length; j++) {
              if (j == 0) {
                that.graphData[i].patternInstance[j].show = true;
              } else {
                that.graphData[i].patternInstance[j].show = true;
              }
            }
          }
          this.loading = false;
        });
      },
      saveChart(svg, data, id, names) {
        var temp = this.pieChart;
        this.pieChart = null;
        this.pieChart = temp;
        this.pieChart[id] += 1;
        this.pieChartShow[id].push(true);
        console.log(names);
        var name = "";
        for (var i = 0; i < names.length; i++)
            name += names[i].value + " ";
        this.pieChartNames[id].push(name);
        const tooltip = d3.select(".tooltip");
        setTimeout(() => {
          var svg2 = d3.select('#chart' + '-' + id + '-' + this.pieChart[id])
              .append('svg')
              .attr('viewBox', '0 0 400 400')
              .html(svg.html());
          var pie = d3.pie()
              .sort(null)
              .value(function (d) {
                return d.value;
              });
          var arc = svg2.selectAll('.arc')
              .data(pie(data));

          arc.select('path')
              .on('mouseover.tooltip', function (d) {
                console.log(d);
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
        }, 500);
      },
      delete_instance(k, i) {
        this.graphData[k].patternInstance[i].show = false;
        var temp = this.graphData;
        this.graphData = null;
        this.graphData = temp;
      },
      delete_pieChart(k, pie) {
        this.pieChartShow[k][pie] = false;
        var temp = this.pieChartShow;
        this.pieChartShow = null;
        this.pieChartShow = temp;
      },
      changeDataSet(event) {
        this.dataset_loading = true;
        this.graphData = null;
        this.em_cardlist = [];
        this.$axios.get('/sidebar', {
          params: {
            dataset: event
          }
        }).then(response => {
            this.realGraph = response.data;
            this.dataset_loading = false;
        })
      }
    },
    computed: {
      pieChartToShow () {

      }
    }
  };

</script>

<style>
    element.style {
        background-color: rgb(10, 47, 88);
    }

    body {
        font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
        "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
        margin: 0;
        display: flex;
        min-height: 100%;
        flex-direction: column;
        height: 100%;
        overflow: hidden;
    }

    #analyze_part,
    #mainrow,
    .graph_container {
        height: 100%;
    }

    #mainpage {
        height: 100%;
        background-color: #eef1f6;
    }

    .el-menu,
    body,
    html {
        height: 100%;
    }

    .wrapper {
        position: relative;
    }

    div {
        display: block;
    }

    .container {
        padding-top: 70px;
        flex: 1;
    }

    .container,
    .app {
        height: 100%;
    }

    .wrapper {
        height: 95%;
    }

    .menu {
        text-align: center;
        height: 100%;
        background-color: #eef1f6;
    }

    .content {
        height: 100%;
    }

    #dataset_select {
        z-index: 1;
        margin-top: 10px;
        position: relative;
    }

    .graph-card /deep/ .el-card__header {
        text-align: initial;
        height: 30px;
        padding: 4px 5px;
    }

    .side_card /deep/ .el-card__header {
        text-align: initial;
        height: 30px;
        padding: 4px 5px;
        font-size: smaller;
    }

    #analyze_card /deep/ .el-card__header {
        text-align: initial;
        height: 50px;
        padding: 4px 10px;
    }

    .chart-header /deep/ .el-card__header {
        text-align: initial;
        height: 30px;
        padding: 4px 5px;
    }

    .side_card {
        width: 90%;
        margin-left: 5%;
        margin-top: 5px;
    }

    .item {
        margin-bottom: 10px;
    }

    .clearfix:before,
    .clearfix:after {
        display: table;
        content: "";
    }

    .clearfix:after {
        clear: both;
    }

    .el-input__prefix,
    .el-input__suffix {
        top: 5px;
    }

    .sidebar_btn {
        width: 100%;
    }

    .el-card__body {
        padding: 10px;
    }

    #analyze_part {
        /* height: 100%; */
        overflow: hidden;
        position: relative;
        background-color: #eef1f6;
    }

    #analyze_part .analyze_div {
        height: 100%;
        left: 0px;
        right: 5px;
        top: 65px;
        bottom: 0px;
        /* width: 100%; */
    }

    .el-collapse-item__header {
        font-size: 15px;
        padding-left: 10px;
        padding-right: 10px;
    }

    #analyze_card {
        left: 0px;
        right: 5px;
        top: 70px;
        /* width: 100%; */
        height: 100%;
        position: absolute;
        overflow: auto;
        overflow-x: hidden;
    }

    .el-input__suffix {
        z-index: 4;
    }

    #result_part {
        top: 70px;
        bottom: 10px;
        height: 89%;
        overflow: hidden;
        position: relative;
        /*background-color: green;*/
    }

    .footer {
        clear: both;
        position: relative;
        height: 180px;
        margin-top: -180px;
        background-color: rgba(0, 63, 67, 0.8);
    }

    #result_part .graph_container {
        left: 0;
        top: 0;
        bottom: 0;
        width: 100%;
        position: absolute;
        overflow: auto;
        overflow-x: hidden;
    }
</style>
