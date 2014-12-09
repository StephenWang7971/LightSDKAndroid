package cn.alphabets.light.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 后台请求的结果对象，处理JSON与数据的转换
 * Created by luohao on 14/10/17.
 */
public class ResponseParser<T> {

    /** API版本 */
    private String apiVersion;

    /** 标识 */
    private String id;

    /** HTTP请求方法 */
    private String method;

    /** 一览信息 */
    private Data<T> data;

    /** 附加的名称等信息 */
    private JSONObject options;

    /** 获取单条信息时的详细信息 */
    private T detail;

    /** 错误信息 */
    private Error error;

    /**
     * JSON转对象
     * @param json 待转换的JSON对象
     * @param type Mod类型的TypeToken
     * @param <C> Mod类型
     * @return 转换后的类
     */
    public static <C> ResponseParser<C> fromJson(JSONObject json, TypeToken type) {

        Gson gson = new Gson();

        try {
            ResponseParser<C> result = new ResponseParser<C>();
            if (json.has("apiVersion")) {
                result.setApiVersion(json.getString("apiVersion"));
            }

            // 后台处理出错
            if (json.has("error")) {
                if (json.has("error")) {
                    String errorString = json.getJSONObject("error").toString();
                    Error error = gson.fromJson(errorString, Error.getTypeToken().getType());
                    result.setError(error);
                }
                return result;
            }

            // 如果没有data字段，返回空（数据格式错误等）
            if (!json.has("data")) {
                return null;
            }

            JSONObject jsonData = json.getJSONObject("data");

            // 一览格式的数据
            if (jsonData.has("items")) {

                List<C> items = gson.fromJson(jsonData.getString("items"), type.getType());
                Data<C> data = new Data<C>();
                data.setItems(items);
                data.setTotalItems(jsonData.getInt("totalItems"));
                result.setData(data);

                if (jsonData.has("options")) {
                    result.setOptions(jsonData.getJSONObject("options"));
                }
                return result;
            }

            // 单一格式的数据
            else {

                result.setDetail(gson.<C>fromJson(jsonData.toString(), type.getType()));

                if (jsonData.has("options")) {
                    result.setOptions(jsonData.getJSONObject("options"));
                }
                return result;
            }
        } catch (JSONException e) {
            throw new RuntimeException("Converting object failed.");
        }

    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Data<T> getData() {
        return data;
    }

    public void setData(Data<T> data) {
        this.data = data;
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public JSONObject getOptionsUser() throws JSONException {
        return this.getOptions().getJSONObject("user");
    }

    public JSONObject getOptionsGroup() throws JSONException {
        return this.getOptions().getJSONObject("group");
    }

    public JSONObject getOptionsCategory() throws JSONException {
        return this.getOptions().getJSONObject("category");
    }

    public T getDetail() {
        return detail;
    }

    public void setDetail(T detail) {
        this.detail = detail;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public boolean hasError() {
        return this.error != null;
    }

    /**
     * 数据详细
     * @param <R>
     */
    public static class Data<R> {

        private int totalItems;
        private List<R> items;

        public int getTotalItems() {
            return totalItems;
        }

        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }

        public List<R> getItems() {
            return items;
        }

        public void setItems(List<R> items) {
            this.items = items;
        }
    }

    /**
     * 错误数据
     */
    public static class Error {
        private String message;
        private String code;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public static TypeToken getTypeToken() {
            return new TypeToken<Error>() {};
        }
    }
}
