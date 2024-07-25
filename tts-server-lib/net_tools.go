package tts_server_lib

import (
	tts_server_go "github.com/jing332/tts-server-go"
	"net/http"
	"strings"
//     "bytes"
//     "encoding/json"
//     "fmt"
//     "io/ioutil"
//      "log"
//     "time"
)

func UploadLog(logstr string) (string, error) {
	uploadUrl := "https://bin.kv2.dev"
	req, err := http.NewRequest(http.MethodPost, uploadUrl, strings.NewReader(logstr))
	if err != nil {
		return "", err
	}

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	return uploadUrl + res.Request.URL.Path, nil
}

func GetOutboundIP() string {
	return tts_server_go.GetOutboundIPString()
}
// func GetOutboundIP2(id, book, author, title, text, data string) (string, error) {
// 	return Fetch(id, book, author, title, text, data)
// }
// func GetRelpaceRuler(id, book, author, title, tts, data string) (string, error) {
// 	return FetchRelpaceRuler(id, book, author, title, tts, data)
// }

type UploadConfigJson struct {
	Data string `json:"data"`
	Msg  string `json:"msg"`
}
