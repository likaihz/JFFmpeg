import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FFMpegUtil implements IStringGetter {

    //private final int Step = 5;
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
     * 视频切片
     */
    public void videoSlice(int startTime, int length, int step)
    {
        for(int i = 0; i * step < length; i++)
        {
            System.out.print(i);
            videoPart(startTime+i*step, step, originVideoUri+Integer.toString(i));
            System.out.println(0);
        }
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
                Matcher m = Pattern.compile("Duration: //w+://w+://w+").matcher(str);
                while (m.find())
                {
                    String msg = m.group();
                    msg = msg.replace("Duration: ", "");
                    //runtime = TimeUtil.runtimeToSecond(msg);
                }
                break;
            }
        }//switch
    }
}
