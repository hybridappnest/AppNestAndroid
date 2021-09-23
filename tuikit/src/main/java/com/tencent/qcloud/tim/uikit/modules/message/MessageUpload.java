package com.tencent.qcloud.tim.uikit.modules.message;

public class MessageUpload {
    String fromAccount;
    long msgTimeStamp;
    String MsgType;
    TextContent textContent;
    ImageContent imageContent;
    VoiceContent voiceContent;
    VideoContent videoContent;
    CustomContent customContent;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public long getMsgTimeStamp() {
        return msgTimeStamp;
    }

    public void setMsgTimeStamp(long msgTimeStamp) {
        this.msgTimeStamp = msgTimeStamp;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public TextContent getTextContent() {
        if(textContent == null){
            return  new TextContent();
        }
        return textContent;
    }

    public void setTextContent(TextContent textContent) {
        this.textContent = textContent;
    }

    public ImageContent getImageContent() {
        if(imageContent == null){
            return  new ImageContent();
        }
        return imageContent;
    }

    public void setImageContent(ImageContent imageContent) {
        this.imageContent = imageContent;
    }

    public VoiceContent getVoiceContent() {
        if(voiceContent == null){
            return  new VoiceContent();
        }
        return voiceContent;
    }

    public void setVoiceContent(VoiceContent voiceContent) {
        this.voiceContent = voiceContent;
    }

    public VideoContent getVideoContent() {
        if(videoContent == null){
            return  new VideoContent();
        }
        return videoContent;
    }

    public void setVideoContent(VideoContent videoContent) {
        this.videoContent = videoContent;
    }

    public CustomContent getCustomContent() {
        if(customContent == null){
            return  new CustomContent();
        }
        return customContent;
    }

    public void setCustomContent(CustomContent customContent) {
        this.customContent = customContent;
    }

    public class TextContent{
        String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
           this.text = text;
        }
    }
    public class ImageContent{
        String url;
        int width;
        int height;
        String thumbUrl;
        int thumbWidth;
        int thumbHeight;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getThumbUrl() {
            return thumbUrl == null ? "" : thumbUrl;
        }

        public void setThumbUrl(String thumbUrl) {
            this.thumbUrl = thumbUrl;
        }

        public int getThumbWidth() {
            return thumbWidth;
        }

        public void setThumbWidth(int thumbWidth) {
            this.thumbWidth = thumbWidth;
        }

        public int getThumbHeight() {
            return thumbHeight;
        }

        public void setThumbHeight(int thumbHeight) {
            this.thumbHeight = thumbHeight;
        }
    }
    public class VoiceContent{
        String url;
        String discernResult;
        int duration;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDiscernResult() {
            return discernResult;
        }

        public void setDiscernResult(String discernResult) {
            this.discernResult = discernResult;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
    public class VideoContent{
        String url;
        String snapshotPath;
        int duration;
        int width;
        int height;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSnapshotPath() {
            return snapshotPath;
        }

        public void setSnapshotPath(String snapshotPath) {
            this.snapshotPath = snapshotPath;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
    public class CustomContent{
        String data;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}