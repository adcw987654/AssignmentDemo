# AmpGo 示範專案

這是一個 Spring Boot 應用程式的示範專案。它展示了一個簡單的用戶註冊和登入系統，並包含電子郵件驗證功能。

## 使用技術

*   **後端：**
    *   Java 21
    *   Spring Boot 3
    *   Spring Web
    *   Spring Security (用於密碼加密)
    *   MyBatis (用於資料庫存取)
    *   H2 資料庫 (記憶體資料庫)
    *   JJWT (用於 JSON Web Token 的生成和驗證)
    *   Spring Mail (用於發送驗證郵件)
*   **API 文件：**
    *   SpringDoc (Swagger UI)
*   **建置工具：**
    *   Maven

## 專案結構

該專案遵循標準的 Maven 專案結構：

```
.
├── src
│   ├── main
│   │   ├── java/com/ampgo/demo
│   │   │   ├── controller    # 處理傳入的 HTTP 請求 (例如：登入、註冊)
│   │   │   ├── service       # 包含業務邏輯
│   │   │   ├── entity        # 代表資料庫資料表的實體
│   │   │   ├── mapper        # 用於資料庫操作的 MyBatis 映射器
│   │   │   ├── dto           # 用於 API 請求和回應的資料傳輸物件
│   │   │   ├── config        # Spring 配置類別 (例如：Swagger、Async)
│   │   │   ├── event         # 應用程式事件 (例如：UserRegisteredEvent)
│   │   │   ├── listener      # 事件監聽器 (例如：用於在註冊時發送電子郵件)
│   │   │   ├── exception     # 自訂例外類別
│   │   │   └── handler       # 全域例外處理器
│   │   └── resources
│   │       ├── static        # 靜態資源，如 HTML、CSS、JS
│   │       ├── templates     # 電子郵件範本 (例如：verification-email.html)
│   │       ├── application.yml # Spring Boot 設定檔
│   │       └── schema.sql      # H2 資料庫的結構定義
│   └── test                  # 測試原始碼
├── pom.xml                   # Maven 專案配置
└── readme.md                 # 本檔案
```

## 如何執行

1.  複製儲存庫。
2.  使用 Maven 建置專案 (這會編譯程式碼、執行測試並將應用程式打包成一個 JAR 檔案):
    ```bash
    ./mvnw clean package
    `````
3.  執行應用程式：
    ```bash
    java -jar target/demo-0.0.1-SNAPSHOT.jar
    ```
4.  應用程式將在 `http://localhost:8080` 上提供服務。

## 端點

### API 文件 (Swagger UI)

API 文件是使用 SpringDoc 產生的，可透過以下 URL 存取。您可以使用此介面來探索和測試可用的 API 端點。

*   [Swagger UI](http://ec2-18-183-225-68.ap-northeast-1.compute.amazonaws.com:8080/swagger-ui/index.html)

### 示範應用程式

一個簡單的前端應用程式可在以下位置找到：

*   [示範首頁](http://ec2-18-183-225-68.ap-northeast-1.compute.amazonaws.com:8080/index.html)

