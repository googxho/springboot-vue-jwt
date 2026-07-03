package com.example.entity;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * 统一响应体 —— 后端返回给前端的数据，都套上这个壳。
 * <p>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  返回的 JSON 格式：                                             │
 * │  {                                                             │
 * │    "id":      123456,     ← 请求ID（用于追踪日志）              │
 * │    "code":    200,        ← 状态码（200成功/401未登录/403无权限）│
 * │    "data":    {...},      ← 真正的业务数据                      │
 * │    "message": "请求成功"   ← 提示信息                           │
 * │  }                                                             │
 * └─────────────────────────────────────────────────────────────────┘
 * <p>
 * ★ 关于 Java record 类型（Java 16+）：
 *                                                                   ┌──────────────────────────────┐
 *    record 是 Java 提供的一种"纯数据容器"，相当于                │  传统写法（约 60 行）          │
 *    编译器帮你自动生成了一大堆模板代码。                          │                              │
 *                                                                  │  class RestBean {             │
 *    这一行：                                                       │    private int code;          │
 *      record RestBean(long id, int code, T data, String msg) {}   │    public int getCode() {...} │
 *                                                                  │    public void setCode() {...} │
 *    等价于右边那一大坨（约 60 行代码）。                           │    public boolean equals(){...}│
 *                                                                  │    public int hashCode(){...}  │
 *    record 自动生成的内容：                                        │    public String toString(){.} │
 *    ├─ 构造函数（带所有参数）                                     │    // ... 还有几十行          │
 *    ├─ Getter（但叫 code() 而不是 getCode()）                     │  }                             │
 *    ├─ equals() / hashCode()                                      └──────────────────────────────┘
 *    ├─ toString()
 *    └─ 字段全是 private final（不可变）
 *                                                                  ──── 用 record 后只剩 1 行 ────
 *    record 解决的问题：                                            record RestBean(long id, int code, T data, String msg) {}
 *    每次写"只用来装数据的类"，都要重复写：
 *    构造函数、getter/setter、equals、hashCode、toString...
 *    要么自己手写（累），要么用 Lombok 的 @Data（引入外部依赖）。
 *    record 是 Java 官方的内置方案，零依赖，开箱即用。
 *
 * @param id      请求ID，用于日志追踪（每个请求唯一）
 * @param code    状态码（200=成功，401=未登录，403=无权限）
 * @param data    真正的响应数据（泛型 T，可以是任何类型）
 * @param message 提示消息
 * @param <T>     响应数据的类型
 */
@Schema(description = "统一响应体")
public record RestBean<T>(
        @Schema(description = "请求ID，用于日志追踪（每个请求唯一）") long id,
        @Schema(description = "状态码：200=成功，401=未登录，403=无权限，404=未找到") int code,
        @Schema(description = "业务数据") T data,
        @Schema(description = "提示信息") String message) {

    /**
     * 成功响应 —— 带业务数据。
     * <p>
     * 使用示例：
     *   return RestBean.success(userList);    // 返回用户列表
     *   return RestBean.success("操作完成");
     * <p>
     * 前端收到的 JSON：
     *   { "id": 1001, "code": 200, "data": [...], "message": "请求成功" }
     *
     * @param data 要返回给前端的业务数据
     * @param <T>  数据类型
     * @return RestBean 对象（code=200）
     */
    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(requestId(), 200, data, "请求成功");
    }

    /**
     * 成功响应 —— 无业务数据（仅表示"操作成功"）。
     * <p>
     * 使用示例：
     *   return RestBean.success();    // 只告诉前端"成功了"，不返回具体数据
     */
    public static <T> RestBean<T> success() {
        return success(null);
    }

    /**
     * 403 禁止访问 —— 用户没有权限执行此操作。
     * <p>
     * 使用示例：
     *   return RestBean.forbidden("您不是管理员，无法删除用户");
     * <p>
     * 前端收到的 JSON：
     *   { "id": 1001, "code": 403, "data": null, "message": "您不是管理员..." }
     *
     * @param message 具体的错误原因
     * @param <T>     数据类型
     * @return RestBean 对象（code=403）
     */
    public static <T> RestBean<T> forbidden(String message) {
        return failure(403, message);
    }

    /**
     * 401 未登录 —— 用户未提供有效的登录凭证。
     * <p>
     * 使用示例：
     *   return RestBean.unauthorized("请先登录");
     * <p>
     * 前端收到的 JSON：
     *   { "id": 1001, "code": 401, "data": null, "message": "请先登录" }
     *
     * @param message 具体的错误原因
     * @param <T>     数据类型
     * @return RestBean 对象（code=401）
     */
    public static <T> RestBean<T> unauthorized(String message) {
        return failure(401, message);
    }

    /**
     * 通用失败响应 —— 可自定义状态码和错误信息。
     * <p>
     * 使用示例：
     *   return RestBean.failure(400, "参数格式不正确");
     *   return RestBean.failure(500, "服务器内部错误");
     *
     * @param code    状态码（如 400/403/404/500 等）
     * @param message 具体的错误信息
     * @param <T>     数据类型
     * @return RestBean 对象
     */
    public static <T> RestBean<T> failure(int code, String message) {
        return new RestBean<>(requestId(), code, null, message);
    }

    /**
     * 将当前 RestBean 转为 JSON 字符串（用于直接写入 HTTP 响应）。
     * <p>
     * 使用示例：
     *   response.getWriter().write(RestBean.success(data).asJsonString());
     * <p>
     * 输出示例：
     *   {"id":1001,"code":200,"data":{"name":"小明"},"message":"请求成功"}
     *
     * @return JSON 格式的字符串
     */
    public String asJsonString() {
        return JSONObject.toJSONString(this, JSONWriter.Feature.WriteNulls);
    }

    /**
     * 获取当前请求的唯一 ID，用于在日志中追踪某一次请求的全链路。
     * <p>
     * 这个 ID 来自日志框架 MDC（Mapped Diagnostic Context），
     * 在请求进入时由过滤器（如 RequestLogFilter）生成并存入 MDC，
     * 这样同一个请求经过的所有代码都能拿到相同的 ID，
     * 方便把分散的日志串起来看。
     *
     * @return 请求 ID（如果 MDC 中没有 reqId，则返回 0）
     */
    private static long requestId() {
        // 从 MDC 中取出请求 ID，如果没有就默认 0
        String requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0");
        return Long.parseLong(requestId);
    }
}

