import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FFMpegUtil implements IStringGetter{

    private int runtime;
    private String ffmpegUri;
    private String originVideoUri;
    private enum FFMpegUtilStatus { Empty, CheckingFile, GettingRuntime };
    private FFMpegUtilStatus status = FFMpegUtilStatus.Empty;
    /**
     * 构造函数
     * @param ffmpegUri FFmpeg的文件路径
     * @param videoUri 待处理视频的文件路径
     */
    public FFMpegUtil(String ffmpegUri, String videoUri)
    {
        this.ffmpegUri = ffmpegUri;
        this.originVideoUri = videoUri;
    }

    /**
     * 获取视频时长
     */
    public int getRuntime()
    {
        runtime = 0;
        status = FFMpegUtilStatus.GettingRuntime;
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        CmdExecuter.exec(cmd, this);
        return runtime;

    }

    /**
     *
     */
    public boolean isSupported()
    {
        isSupported = true;
        status = FFMpegUtilStatus.CheckingFile;
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        CmdExecuter.exec(cmd, this);
        return isSupported;
    }
    private boolean isSupported;

    private List<String> cmd = new ArrayList<>();

    /**
     * 生成视频截图
     * @param time 截图时间
     * @param imageSavePath 截图文件保存全路径
     * @param screenSize 截图大小 如640x480
     */
    public void makeScreenCut(int time, String imageSavePath , String screenSize ){
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-ss");
        cmd.add(Integer.toString(time));
        cmd.add("-i");
        cmd.add(originVideoUri);
        cmd.add("-y");
        cmd.add("-f");
        cmd.add("image2");
        cmd.add("-t");
        cmd.add("0.001");
        cmd.add("-s");
        cmd.add(screenSize);
        cmd.add(imageSavePath);
        CmdExecuter.exec(cmd, null);
    }


    /**
     * 视频转换、压缩等
     * @param fileSavePath 文件保存全路径（包括扩展名）如 e:/abc/test.flv
     * @param screenSize 视频分辨率 如640x480
     * @param audioByte 音频比特率
     * @param audioCollection 音频采样率
     * @param quality 视频质量(0.01-255)越低质量越好
     * @param fps 每秒帧数（15或29.97）
     */
    public void videoTransfer(String fileSavePath,
                              String screenSize,
                              int audioByte,
                              int audioCollection,
                              double quality,
                              double fps )
    {
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        cmd.add("-y");
        cmd.add("-ab");
        cmd.add( Integer.toString(audioByte) );
        cmd.add("-ar");
        cmd.add( Integer.toString(audioCollection) );
        cmd.add("-qscale");
        cmd.add( Double.toString(quality) );
        cmd.add("-r");
        cmd.add( Double.toString(fps) );
        cmd.add("-s");
        cmd.add(screenSize);
        cmd.add(fileSavePath);
        CmdExecuter.exec(cmd, null);
    }


    /**
     * 压缩视频，将视频码率压缩到5000kb/s左右
     * @param fileSavePath 输出文件存储路径
     */
    public void videoCompress(String fileSavePath)
    {
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        cmd.add("-b");
        cmd.add("5000000");
        cmd.add(fileSavePath);
        CmdExecuter.exec(cmd, null);
    }
    /**
     * 切割视频
     * @param startTime 片段开始时间
     * @param partLength 片段时长
     * @param fileSavePath 输出片段保存路径
     */

    public void videoPart(int startTime, int partLength, String fileSavePath)
    {
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        cmd.add("-ss");
        cmd.add(Integer.toString(startTime));
        cmd.add("-t");
        cmd.add(Integer.toString(partLength));
        cmd.add("-acodec");
        cmd.add("copy");
        cmd.add("-vcodec");
        cmd.add("copy");
        cmd.add(fileSavePath);
        CmdExecuter.exec(cmd, null);

    }
    /**
     * 视频切片，生成m3u8支持的格式
     * @param startTime 开始时间
     * @param length 待处理片段长度
     * @param fileSaveDir 文件保存目录
     * @param fileSaveName 输出文件名称
     */
    public void videoSlice(int startTime, int length, String fileSaveDir, String fileSaveName)
    {
        File f = new File(fileSaveDir);
        if(!f.exists()) f.mkdirs();
        cmd.clear();
        cmd.add(ffmpegUri);
        cmd.add("-i");
        cmd.add(originVideoUri);
        cmd.add("-ss");
        cmd.add(Integer.toString(startTime));
        cmd.add("-t");
        cmd.add(Integer.toString(length));
        cmd.add("-acodec");
        cmd.add("copy");
        cmd.add("-vcodec");
        cmd.add("copy");
        cmd.add("-strict");
        cmd.add("-2");
        cmd.add("-f");
        cmd.add("hls");
        cmd.add("-hls_list_size");
        cmd.add("0");
        cmd.add(fileSaveDir+fileSaveName+".m3u8");
        CmdExecuter.exec(cmd, null);
    }
    /**
     * 处理控制台输出信息
     */
    @Override
    public void dealString( String str ){

        switch( status )
        {
            case Empty:
                break;
            case CheckingFile:{
                Matcher m = Pattern.compile("Invalid data found when processing input").matcher(str);
                if( m.find() )
                    this.isSupported = false;
                break;
            }
            case GettingRuntime:{
                String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
                Pattern pattern = Pattern.compile(regexDuration);
                Matcher m = pattern.matcher(str);
                if(m.find())
                {
                    runtime = getTimelen(m.group(1));
                }
            }
        }//switch
    }

    private static int getTimelen(String timelen){
        int min=0;
        String strs[] = timelen.split(":");
        if (strs[0].compareTo("0") > 0) {
            min+=Integer.valueOf(strs[0])*60*60;//秒
        }
        if(strs[1].compareTo("0")>0){
            min+=Integer.valueOf(strs[1])*60;
        }
        if(strs[2].compareTo("0")>0){
            min+=Math.round(Float.valueOf(strs[2]));
        }
        return min;
    }
}
