<template>
  <div id="mainpage">
    <el-row id="mainrow" :gutter="4">
      <el-col :span="8" id="analyze_part">
        <div class="analyze_div">
          <el-card class="box-card" id="analyze_card" shadow="never">
            <div slot="header" class="clearfix" style="height:30px">
              <span>pattern分析</span>
              <!-- <el-button style="padding: 3px 0" label="text" @click="show_graph(0)">show test</el-button>
            <el-button style="padding: 3px 0" label="text" @click="init_graph(0)">graph test</el-button>
              <el-button style="float: right; padding: 3px 0" label="text" @click="add_acard">新增</el-button>-->
            </div>
            <div id="analyze_svg">
              <div v-for="(index, item) in em_cardlist" :key="item">
                <el-card
                  :id="'card' + item"
                  class="em-card"
                  shadow="always"
                  style="height: 280px; text-align: center;"
                >
                  <agraph
                    v-bind:graph-data="graphData"
                    v-bind:graph-id="'agraph'+ em_cardlist.length"
                  ></agraph>
                  <el-button-group style="float: right; z-index: 2;">
                    <el-button size="mini" label="success" round icon="el-icon-check"></el-button>
                    <el-button
                      size="mini"
                      label="danger"
                      round
                      icon="el-icon-close"
                      @click="deleteCard(index)"
                    ></el-button>
                  </el-button-group>
                </el-card>
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
      <el-col :span="16" id="result_part">
        <el-row>
          <div class="graph_container">
            <el-row v-for="i in 4" :gutter="4" v-bind:key="'row' + i">
              <el-col :span="8" v-for="j in 3" v-bind:key="'col' + j" ref="infobox">
                <el-card class="box-card">
                  <div slot="header" class="clearfix">
                    <span style="font-size: 16px">{{'实例' + '-' + i + '-' + j}}</span>
                  </div>
                  <graph v-bind:graph-data="graphData" v-bind:graph-id="'graph'+ '-' + i + '-' + j"></graph>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as d3 from "d3";
import Graph from "./Graph.vue";
import agraph from "./analyzeGraph.vue";

export default {
  name: "mainpage",
  components: {
    Graph,
    agraph
  },
  data: function() {
    return {
      em_cardlist: [1],
      realData: {
        nodes: [
          {
            name: "State",
            id: 0
          },
          {
            name: "C17",
            id: 1
          },
          {
            name: "C18",
            id: 2
          },
          {
            name: "Country",
            id: 3
          },
          {
            name: "C16",
            id: 4
          }
        ],
        links: [
          {
            source: 4,
            target: 3,
            id: 0,
            label: "from_country"
          },
          {
            source: 4,
            target: 2,
            label: "host_is"
          },
          {
            source: 1,
            target: 1,
            label: "from_mutate_branch"
          },
          {
            source: 4,
            target: 0,
            label: "from_division"
          },
          {
            source: 4,
            target: 0,
            label: "from_location"
          },
          {
            source: 0,
            target: 0,
            label: "from_division"
          },
          {
            source: 4,
            target: 1,
            label: "from_mutate_branch"
          },
          {
            source: 0,
            target: 3,
            label: "from_country"
          }
        ]
      },
      graphData: {
        nodes: [
          {
            name: "Peter",
            label: "Person",
            id: 1
          },
          {
            name: "Michael",
            label: "Person",
            id: 2
          },
          {
            name: "Neo4j",
            label: "Database",
            id: 3
          },
          {
            name: "Graph Database",
            label: "Database",
            id: 4
          }
        ],
        links: [
          {
            source: 1,
            target: 2,
            label: "KNOWS",
            since: 2010
          },
          {
            source: 1,
            target: 3,
            label: "FOUNDED"
          },
          {
            source: 2,
            target: 3,
            label: "WORKS_ON"
          },
          {
            source: 3,
            target: 4,
            label: "IS_A"
          }
        ]
      }
    };
  },
  methods: {}
};
</script>

<style>
html,
body,
#mainpage,
#analyze_part,
#mainrow,
.graph_container {
  height: 100%;
}

#mainpage {
  background-color: #eef1f6;
}

#analyze_part {
  /* height: 100%; */
  overflow: hidden;
  position: relative;
  background-color: #eef1f6;
}

#analyze_part .analyze_div {
  height: 100%;
  left: 5px;
  right: 5px;
  bottom: 0px;
  /* width: 100%; */
}

#analyze_card {
  left: 5px;
  right: 5px;
  /* width: 100%; */
  height: 100%;
  position: absolute;
  overflow: hidden;
  overflow-x: hidden;
}

#result_part {
  height: 90%;
  overflow: hidden;
  position: relative;
  /*background-color: green;*/
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

#analyze_card /deep/ .el-card__header {
  height: 30px;
  padding: 4px 5px;
  font-size: medium;
}
</style>
