package org.gallon.rss.controller

import com.google.gson.Gson
import org.gallon.rss.downloader.HttpClientDownloader
import org.gallon.rss.entity.common.JsonResult
import org.gallon.rss.entity.gson.FastNews
import org.gallon.rss.entity.mongo.Article
import org.gallon.rss.entity.mongo.RSS
import org.gallon.rss.processor.BaiduTopProcessor
import org.gallon.rss.util.Const
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import us.codecraft.webmagic.Spider
import us.codecraft.webmagic.pipeline.JsonFilePipeline
import java.util.*


@RestController()
@RequestMapping("refresh")
class RefreshAction(val restTemplate: RestTemplate, val mongoTemplate: MongoTemplate) {

    @GetMapping("baidutop")
    fun baidutop(): JsonResult {
        Spider.create(BaiduTopProcessor())
                .setDownloader(HttpClientDownloader())
//                .addUrl("https://top.baidu.com/buzz?b=341") //今日热点
            .addUrl("https://top.baidu.com/buzz?b=1") //实时热点
                .addPipeline(JsonFilePipeline("./output"))
                .thread(1)
                .run()
        return JsonResult(data = "baidutop refresh done " + Date())
    }

    private var fastNews: FastNews? = null

    @GetMapping("jisu")
    fun refreshBaiduTop(): String {
        if (fastNews == null) {
            println("fastNews == null ")

            val body = restTemplate.getForEntity("http://api.jisuapi.com/news/get?channel=头条&start=0&num=15&appkey=c46797350d31f9c0", String::class.java).body
            if (body != null) {
                fastNews = Gson().fromJson(body, FastNews::class.java)
            }
        }
        var count = 0
        val rss = RSS()
        rss.src = "jisu"
        rss.usage = 3 //测试
        fastNews!!.result.list.forEach {
            val article = Article()
            article.title = it.title.replace(Const.REGEX_IGNORE_2, "")
            article.news_time = it.time
            article.src = it.src
            article.category = it.category
            article.pic = it.pic
            article.html = it.content
            article.url = it.url
            article.weburl = it.weburl
            rss.list.add(article)
            count++
            println(count.toString() + "、" + article.title)
        }
        mongoTemplate.save(rss)
        return "jisu refresh done " + Date()
    }

}