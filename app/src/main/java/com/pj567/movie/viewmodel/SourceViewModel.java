package com.pj567.movie.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.pj567.movie.api.ApiConfig;
import com.pj567.movie.bean.AbsSortXml;
import com.pj567.movie.bean.AbsXml;
import com.pj567.movie.bean.Movie;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class SourceViewModel extends ViewModel {
    public MutableLiveData<AbsSortXml> sortResult;
    public MutableLiveData<AbsXml> listResult;
    public MutableLiveData<AbsXml> searchResult;
    public MutableLiveData<AbsXml> detailResult;

    public SourceViewModel() {
        sortResult = new MutableLiveData<>();
        listResult = new MutableLiveData<>();
        searchResult = new MutableLiveData<>();
        detailResult = new MutableLiveData<>();
    }

    public void getSort() {
        OkGo.<String>get(ApiConfig.get().getBaseUrl())
                .tag(ApiConfig.get().getBaseUrl())
                .execute(new AbsCallback<String>() {
                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        if (response.body() != null) {
                            return response.body().string();
                        } else {
                            throw new IllegalStateException("网络请求错误");
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String xml = response.body();
                        sotXml(sortResult, xml);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sortResult.postValue(null);
                    }
                });
    }

    public void getList(int id, int page) {
        OkGo.<String>get(ApiConfig.get().getBaseUrl())
                .tag(ApiConfig.get().getBaseUrl())
                .params("ac", "videolist")
                .params("t", id)
                .params("pg", page)
                .execute(new AbsCallback<String>() {

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        if (response.body() != null) {
                            return response.body().string();
                        } else {
                            throw new IllegalStateException("网络请求错误");
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String xml = response.body();
                        xml(listResult, xml, ApiConfig.get().getBaseUrl(), ApiConfig.get().getDefaultSourceBean().getName());
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        listResult.postValue(null);
                    }
                });
    }

    public void getSearch(String api, String wd, String sourceName) {
        OkGo.<String>get(api)
                .params("wd", wd)
                .tag("search")
                .execute(new AbsCallback<String>() {
                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        if (response.body() != null) {
                            return response.body().string();
                        } else {
                            throw new IllegalStateException("网络请求错误");
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String xml = response.body();
                        xml(searchResult, xml, api, sourceName);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        searchResult.postValue(null);
                    }
                });
    }

    public void getDetail(String api, int id) {
        OkGo.<String>get(api)
                .tag("detail")
                .params("ac", "videolist")
                .params("ids", id)
                .execute(new AbsCallback<String>() {

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        if (response.body() != null) {
                            return response.body().string();
                        } else {
                            throw new IllegalStateException("网络请求错误");
                        }
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        String xml = response.body();
                        xml(detailResult, xml, api, "");
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        detailResult.postValue(null);
                    }
                });
    }

    private void sotXml(MutableLiveData<AbsSortXml> result, String xml) {
        try {
            XStream xstream = new XStream(new DomDriver());//创建Xstram对象
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(AbsSortXml.class);
            xstream.ignoreUnknownElements();
            AbsSortXml data = (AbsSortXml) xstream.fromXML(xml);
            result.postValue(data);
        } catch (Exception e) {
            result.postValue(null);
        }
    }

    private void xml(MutableLiveData<AbsXml> result, String xml, String api, String sourceName) {
        try {
            XStream xstream = new XStream(new DomDriver());//创建Xstram对象
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(AbsXml.class);
            xstream.ignoreUnknownElements();
            if (xml.contains("<year></year>")) {
                xml = xml.replace("<year></year>", "<year>0</year>");
            }
            if (xml.contains("<state></state>")) {
                xml = xml.replace("<state></state>", "<state>0</state>");
            }
            AbsXml data = (AbsXml) xstream.fromXML(xml);
            data.api = api;
            if (data.movie != null && data.movie.videoList != null) {
                for (Movie.Video video : data.movie.videoList) {
                    if (video.urlBean != null && video.urlBean.infoList != null) {
                        for (Movie.Video.UrlBean.UrlInfo urlInfo : video.urlBean.infoList) {
                            String[] str = null;
                            if (urlInfo.urls.contains("#")) {
                                str = urlInfo.urls.split("#");
                            } else {
                                str = new String[]{urlInfo.urls};
                            }
                            List<Movie.Video.UrlBean.UrlInfo.InfoBean> infoBeanList = new ArrayList<>();
                            int i = 1;
                            for (String s : str) {
                                if (s.contains("$")) {
                                    infoBeanList.add(new Movie.Video.UrlBean.UrlInfo.InfoBean(s.substring(0, s.indexOf("$")), s.substring(s.indexOf("$") + 1)));
                                }else{
                                    infoBeanList.add(new Movie.Video.UrlBean.UrlInfo.InfoBean("第" + i + "集", s));
                                    i = i + 1;
                                }
                            }
                            urlInfo.beanList = infoBeanList;
                        }
                    }
                    video.api = api;
                    video.sourceName = sourceName;
                }
            }
            result.postValue(data);
        } catch (Exception e) {
            result.postValue(null);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        OkGo.getInstance().cancelTag(ApiConfig.get().getBaseUrl());
        OkGo.getInstance().cancelTag("search");
        OkGo.getInstance().cancelTag("detail");
    }
}