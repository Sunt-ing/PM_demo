<template>
    <header style="background-color: rgba(0, 63, 67, 0.8); text-align: center;">
        <div class="text-logo">{{msg}}</div>
        <p class="meta" id="dbg"><br>DBGroup @ SUSTech</p>
        <img id="sustech_img" :src="url">
    </header>
</template>

<script>
    // import Mainpage from './components/Mainpage.vue'

    export default {
        name: 'db-header',
        // components: {Mainpage},
        data () {
            return {
                msg: 'Frequent Pattern Discovery on COVID-19 Knowledge Graph',
                url: '../../static/LOGO.png'
            }
        },
        mounted: function () {
            // this.$http.get('../../static/LOGO.png');
        }
    }
</script>

<style>
    header {
        height: 60px;
        position: absolute;
        width: 100%;
        top: 0;
        padding: 0 20px;
        z-index: 1;
        box-sizing: border-box;
        overflow: hidden;
    }

    .text-logo {
        display: inline-block;
        vertical-align: middle;
        border-style: none;
        position: relative;
        top: 16px;
        right: -20px;
        font-size: 25px;
        color: white;

    }

    .meta {
        color: #ffffff;
        width: 220px;
        /* display: block; */
        /* margin: -18px 0 0 385px; */
        font-weight: 700;
        font-size: 0.9rem;
    }

    #dbg {
        bottom: 0px;
        height: 30px;
        left: 42%;
        position: fixed;
        right: 0px;
    }

    #sustech_img {
        position: absolute;
        right: -35px;
        top: -90px;
        height: 240px;
    }
</style>
