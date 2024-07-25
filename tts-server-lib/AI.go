package tts_server_lib

import (
	"fmt"
	"net/http"
    "bytes"
    "encoding/json"
    "io/ioutil"
 	"time"
//      "log"
)

type RequestData struct {
    Id   string `json:"id"`
    Book   string `json:"book"`
    Author string `json:"author"`
    Title  string `json:"chapter"`
    Text   string `json:"text"`
    Data string `json:"data"`
}

type ResponseData struct {
    Text string `json:"text"`
}

func FetchAIChat(id, book, author, title, text ,data string) (string, error) {
    url := "http://home.whya.top:47860/ai_chat_txt"

    // 直接创建 JSON 字符串
    jsonStr := fmt.Sprintf(`{
        "id": %q,
        "book": %q,
        "author": %q,
        "chapter": %q,
        "text": %q,
        "data": %q
    }`, id, book, author, title, text, data)
    // Create a new HTTP request
    req, err := http.NewRequest("POST", url, bytes.NewBufferString(jsonStr))
    if err != nil {
        return "", fmt.Errorf("error creating request: %v", err)
    }
    req.Header.Set("Content-Type", "application/json")

    // Send the request
    client := &http.Client{
        Timeout: 60 * time.Second, // 增加到 60 秒
    }
    resp, err := client.Do(req)
    if err != nil {
        return "", fmt.Errorf("error sending request: %v", err)
    }
    defer resp.Body.Close()

    // Read the response
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        return "", fmt.Errorf("error reading response: %v", err)
    }

    // Unmarshal the response data
    var responseData ResponseData
    err = json.Unmarshal(body, &responseData)
    if err != nil {
        return "", fmt.Errorf("error unmarshaling response data: %q err:%v", body, err)
    }

    return responseData.Text, nil
}
func FetchRelpaceRuler(id, book, author, title, tts, data string) (string, error) {
    url := "http://home.whya.top:47860/replace_ruler"

    // 直接创建 JSON 字符串
    jsonStr := fmt.Sprintf(`{
        "id": %q,
        "book": %q,
        "author": %q,
        "chapter": %q,
        "tts": %q,
        "data": %q
    }`, id, book, author, title, tts, data)

    // 创建一个新的 HTTP 请求
    req, err := http.NewRequest("POST", url, bytes.NewBufferString(jsonStr))
    if err != nil {
        return "", fmt.Errorf("error creating request: %v", err)
    }
    req.Header.Set("Content-Type", "application/json")

    // Send the request
    client := &http.Client{
        Timeout: 60 * time.Second, // 增加到 60 秒
    }
    resp, err := client.Do(req)
    if err != nil {
        return "", fmt.Errorf("error sending request: %v", err)
    }
    defer resp.Body.Close()

    // Read the response
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        return "", fmt.Errorf("error reading response: %v", err)
    }

    // Unmarshal the response data
    var responseData ResponseData
    err = json.Unmarshal(body, &responseData)
    if err != nil {
        return "", fmt.Errorf("error unmarshaling response data: %v", err)
    }

    return responseData.Text, nil
}
func FetchNewRelpaceRuler(id, book, author, title, tts, data string) (string, error) {
    url := "http://home.whya.top:47860/new_replace_ruler"

    // 直接创建 JSON 字符串
    jsonStr := fmt.Sprintf(`{
        "id": %q,
        "book": %q,
        "author": %q,
        "chapter": %q,
        "tts": %q,
        "data": %q
    }`, id, book, author, title, tts, data)

    // 创建一个新的 HTTP 请求
    req, err := http.NewRequest("POST", url, bytes.NewBufferString(jsonStr))
    if err != nil {
        return "", fmt.Errorf("error creating request: %v", err)
    }
    req.Header.Set("Content-Type", "application/json")

    // Send the request
    client := &http.Client{
        Timeout: 60 * time.Second, // 增加到 60 秒
    }
    resp, err := client.Do(req)
    if err != nil {
        return "", fmt.Errorf("error sending request: %v", err)
    }
    defer resp.Body.Close()

    // Read the response
    body, err := ioutil.ReadAll(resp.Body)
    if err != nil {
        return "", fmt.Errorf("error reading response: %v", err)
    }

    // Unmarshal the response data
    var responseData ResponseData
    err = json.Unmarshal(body, &responseData)
    if err != nil {
        return "", fmt.Errorf("error unmarshaling response data: %v", err)
    }

    return responseData.Text, nil
}