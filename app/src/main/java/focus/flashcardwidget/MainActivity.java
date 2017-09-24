package focus.flashcardwidget;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TO MAKE PRO VERSION:
 * Have top 1000 words in chinese and japan
 * Have more than 20 words for custom
 */
public class MainActivity extends AppCompatActivity {

    public final static String DEFAULT_DEFINITIONS_FILENAME = "chinese-test";
    public final static String CHOSENFILENAME = "chosen-file";

    private String[] mFileList;
    private File mPath = new File(Environment.getExternalStorageDirectory() + "//yourdir//");
    private String mChosenFile;
    private static final String FTYPE = ".txt";
    private static final int DIALOG_LOAD_FILE = 1000;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);

        List<String> listOptions = new ArrayList<String>(Arrays.asList("Integrated Chinese Level 1 - Part 1", "Top 100 Chinese Words", "Top 100 Japanese Words", "Add Custom..."));
        ListView listView = (ListView) findViewById(R.id.mainListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listOptions);
        listView.setAdapter(adapter);
        String defaultWords = getIntegratedChinese();
        //setListAdapter(adapter);

        FileOutputStream fos = null;
        try {
            Log.i("TAG", getFilesDir().toString());
            deleteFile(DEFAULT_DEFINITIONS_FILENAME);
            fos = openFileOutput(DEFAULT_DEFINITIONS_FILENAME, Context.MODE_PRIVATE);
            fos.write(defaultWords.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object o = listView.getItemAtPosition(position);
                String str = (String) o; //As you are using Default String Adapter

                //Reset widget


                String chosenWords = "";

                if (position == 0) {
                    chosenWords = getIntegratedChinese();
                    Toast.makeText(getBaseContext(), str + " selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();
                    SimpleWidgetProvider.reset(getBaseContext(), str);
                } else if (position == 1) {
                    chosenWords = getChineseTop100();
                    Toast.makeText(getBaseContext(), str + " selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();
                    SimpleWidgetProvider.reset(getBaseContext(), str);
                } else if (position == 2) {
                    chosenWords = getJapanTop100();
                    Toast.makeText(getBaseContext(), str + " selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();
                    SimpleWidgetProvider.reset(getBaseContext(), str);
                } else if (str.equals("Add Custom...")) {
                    SimpleWidgetProvider.reset(getBaseContext(), str);

                    Log.i("TAG", "IN ADD CUSTOM");

                    chosenWords = getJapanTop100();

                    Intent intent = new Intent()
                            .setType("*/*")
                            .setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
                    return;
                }

                Log.i("TAG", "Chosen words position = " + position);

                FileOutputStream fos = null;
                try {
                    Log.i("TAG", getFilesDir().toString());
                    deleteFile(CHOSENFILENAME);
                    fos = openFileOutput(CHOSENFILENAME, Context.MODE_PRIVATE);
                    fos.write(chosenWords.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }

//    //@Override listview click
//    private void chooseOption() {
//        //IF CHOOSE OPTION 1:
//        if (false) {
//
//            String chosenWords = getIntegratedChinese();
//
//            FileOutputStream fosTwo = null;
//            try {
//                Log.i("TAG", getFilesDir().toString());
//                deleteFile(CHOSENFILENAME);
//                fosTwo = openFileOutput(CHOSENFILENAME, Context.MODE_PRIVATE);
//                fosTwo.write(chosenWords.getBytes());
//                fosTwo.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            Toast.makeText(getBaseContext(), "Custom Flashcards selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();

            Log.i("TAG", "CHOSE CUSTOM FILE: " + selectedfile);

            //File selectedFile =

            InputStream iStream = null;
            try {
                iStream = getContentResolver().openInputStream(selectedfile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] inputData = "".getBytes();

            try {
                inputData = getBytes(iStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos = null;
            try {
                Log.i("TAG", getFilesDir().toString());
                deleteFile(CHOSENFILENAME);
                fos = openFileOutput(CHOSENFILENAME, Context.MODE_PRIVATE);
                fos.write(inputData);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public static String getJapanTop100() {
        String string = "";
        string += "の,indicates possessive\n";
        string += "た,indicates past completed or action; indicates light imperative\n";
        string += "に,indicates location of person or thing\n";
        string += "を,indicates direct object of action\n";
        string += "て,casual quoting particle\n";
        string += "だ,be; is; indicates past or completed action\n";
        string += "が,indicates sentence subject\n";
        string += "と,if; when; and; if; used for quoting\n";
        string += "する,to do\n";
        string += "ない,not;\n";
        string += "いる,to be; to go\n";
        string += "も,also; too\n";
        string += "で,indicates location of action\n";
        string += "か,indicates question (at sentence end)\n";
        string += "ある,to be; to exist\n";
        string += "から,from\n";
        string += "こと,thing\n";
        string += "ん,yes; uh huh\n";
        string += "なる,to become\n";
        string += "う,rabbit\n";
        string += "れる,indicates passive voice\n";
        string += "よう,v. to get drunk\n";
        string += "その,that (something distant from the speaker)\n";
        string += "ます,to increase; to grow\n";
        string += "です,be; is\n";
        string += "それ,that (item near listener)\n";
        string += "よ,evening, night\n";
        string += "そう,so, really\n";
        string += "人,(ひと）person\n";
        string += "いう,to say, to call\n";
        string += "ぱ,apiece, each\n";
        string += "お,honorable\n";
        string += "言う,to say, to name\n";
        string += "もの,person\n";
        string += "この,this (something close to speaker)\n";
        string += "くる,to come\n";
        string += "へ,indicates direction or goal\n";
        string += "ぬ,plain, field\n";
        string += "思う,to think\n";
        string += "一,one\n";
        string += "見る,to see\n";
        string += "何,what\n";
        string += "な,indicates emotion or emphasis\n";
        string += "や,such things as (before a list)\n";
        string += "られる,indicates passive voice\n";
        string += "二,two\n";
        string += "ね,indicates emphasis and/or agreement\n";
        string += "さ,–ness (indicating degree or condition)\n";
        string += "まで,until\n";
        string += "たち,plural suffix for people/animals\n";
        string += "せる,to approach\n";
        string += "く,section\n";
        string += "自分,myself, me\n";
        string += "って,casual quoting particle\n";
        string += "じゃ,then, well\n";
        string += "だけ,only\n";
        string += "てる,to shine\n";
        string += "私,I, me\n";
        string += "中,inside\n";
        string += "さん,formal suffix after person's name\n";
        string += "ながら,while, although\n";
        string += "いく,some\n";
        string += "わ,indicates emotion or admiration\n";
        string += "という,said, called thus\n";
        string += "来る,to come, to arrive\n";
        string += "これ,this (item near speaker)\n";
        string += "顔,face\n";
        string += "できる,to be able to\n";
        string += "いい,good, excellent\n";
        string += "彼,that\n";
        string += "しまう,to finish\n";
        string += "手,(て）hand, arm\n";
        string += "ら,pluralizing suffix\n";
        string += "声,(こえ）voice\n";
        string += "たい,opposite\n";
        string += "目,(め）eye, eyeball\n";
        string += "やる,to send, to dispatch\n";
        string += "どう,how\n";
        string += "前,(さき）previous, prior\n";
        string += "みる,to look after, to see\n";
        string += "でも,but, however\n";
        string += "男,(おとこ）man\n";
        string += "あの,that\n";
        string += "者,(まの) person\n";
        string += "出る,(でる) to leave, to exit\n";
        string += "知る,(しる) to know\n";
        string += "十,ten\n";
        string += "もう,already\n";
        string += "行く,to go\n";
        string += "三,three\n";
        string += "わかる,to understand\n";
        string += "そんな,like that\n";
        string += "くれる,to give\n";
        string += "きる,to kill\n";
        string += "ところ,place, spot\n";
        string += "時,とき\n";
        string += "彼女,woman\n";
        string += "ここ,here\n";
        string += "わたし,I, me\n";
        string += "そして,and then, as such, thus\n";

        return string;

    }

    public static String getIntegratedChinese() {
        String string = "";
        string += "先生,xiānsheng – mr; husband; teacher\n";
        string += "你好,nǐ hǎo – How do you du? Hello!\n";
        string += "小姐,xiǎojie – Miss; young lady\n";
        string += "请问,qǐngwèn – May I ask...\n";
        string += "您,nín – You(singular polite)\n";
        string += "您贵姓,nínguìxìng – What is your honorable surname?\n";
        string += "呢,ne - (an interrogative particle)\n";
        string += "叫,jiào - to be called; to call\n";
        string += "什么,shénme - What?\n";
        string += "名字,míngzi - Name\n";
        string += "是,shì - To be\n";
        string += "老师,Lǎoshī - Teacher\n";
        string += "吗,ma - (an interrogative particle)\n";
        string += "不,bù - not; no\n";
        string += "学生,xuésheng - student\n";
        string += "也,yě - too; also\n";
        string += "中国人,Zhōngguórén - Chinese people/person\n";
        string += "美国人,Měiguórén - American people/person\n";
        string += "那,nà/nèi - that\n";
        string += "张,zhāng - (a measure word for flat objects)\n";
        string += "照片,zhàopiàn - picture; photo\n";
        string += "的,de - (a possessive$ modifying or descriptive particle)\n";
        string += "这,zhè/zhèi - this\n";
        string += "爸爸,bàba - father; dad\n";
        string += "妈妈,māma - mother; mom\n";
        string += "这个,zhège - this\n";
        string += "男孩子,nánháizi - boy\n";
        string += "谁,shéi - who\n";
        string += "他,tā - he; him\n";
        string += "弟弟,dìdi - younger brother\n";
        string += "女孩子,nǚháizi - girl\n";
        string += "妹妹,mèimei - younger sister\n";
        string += "她,tā - she; her\n";
        string += "女儿,nǚ'ér - daughter\n";
        string += "有,yǒu - to have; to exist\n";
        string += "儿子,érzi - son\n";
        string += "没,méi - not\n";
        string += "家,jiā - family; home\n";
        string += "几,jǐ - how many\n";
        string += "哥哥,gēge - older brother\n";
        string += "两,liǎng - two; a couple of\n";
        string += "姐姐,jiějie - older sister\n";
        string += "和,hé - and\n";
        string += "做,zuò - to do\n";
        string += "英文,Yīngwén - the English language\n";
        string += "律师,lǜshī - lawyer\n";
        string += "都,dōu - both; all\n";
        string += "大学生,dàxuéshēng - University; college\n";
        string += "医生,yīshēng - doctor; physician\n";
        string += "九月,jiǔyuè - September\n";
        string += "十二,shí'èr - twelve\n";
        string += "号,hào - number in a series; day of the month\n";
        string += "星期四,xīngqīsì - Thursday\n";
        string += "天,tiān - day\n";
        string += "生日,shēngrì - Birthday\n";
        string += "今年,jīnnián - this year\n";
        string += "多大,duō dà - how old\n";
        string += "十八,shíbā - eighteen\n";
        string += "岁,suì - year (of age)\n";
        string += "请,qǐng - to treat (sombody); to invite\n";
        string += "吃,chī - to eat\n";
        string += "晚饭,wǎnfàn - dinner; supper\n";
        string += "吃饭,chī fàn - to eat (a meal)\n";
        string += "怎么样,zěnmeyàng - Is it OK? What is it like? How does that sound?\n";
        string += "太...了,tài...le - too; extremely\n";
        string += "谢谢,xièxie - thank you\n";
        string += "喜欢,xǐhuan - to like$ like to; to prefer$ prefer to\n";
        string += "还是,háishi - or\n";
        string += "可是,kěshì - but\n";
        string += "好,hǎo - good; OK\n";
        string += "我们,wǒmen - we; us\n";
        string += "点钟,–diǎnzhōng - o'clock\n";
        string += "半,bàn - half; half an hour\n";
        string += "晚上,wǎnshang - evening; night\n";
        string += "见,jiàn - to see\n";
        string += "再见,zàijiàn - good bye; see you again\n";
        string += "现在,xiànzài - now\n";
        string += "刻,–kè - quarter (hour); 15 minutes\n";
        string += "事,shì - matter; affair; business\n";
        string += "明天,míngtiān - tomorrow\n";
        string += "忙,máng - busy\n";
        string += "今天,jiāntiān - today\n";
        string += "很,hěn - very\n";
        string += "为什么,wèishénme - why?\n";
        string += "因为,yīnwèi - because\n";
        string += "还有,háiyǒu - also there are\n";
        string += "同学,tóngxué - classmate\n";
        string += "认识,rènshi - to know (someone); to recognize\n";
        string += "周末,zhōumò - weekend\n";
        string += "打球,dǎ qiú - to play ball\n";
        string += "看,kàn - to watch; to look\n";
        string += "电视,diànshì - TV\n";
        string += "唱歌,chàng gēr - to sing (a song)\n";
        string += "跳舞,tiào wǔ - to dance\n";
        string += "听,tīng - to listen\n";
        string += "音乐,yīnyuè - muisc\n";
        string += "对,duì - right; correct\n";
        string += "有时候,yǒu shíhou - sometimes\n";
        string += "看书,kàn shū - to read books; to read\n";
        string += "电影,diànyǐng - movie\n";
        string += "常常,chángcháng - often\n";
        string += "那,nà - in that case; then\n";
        string += "去,qù - to go\n";
        string += "外国,wàiguó - foreign country\n";
        string += "请客,qǐng kè - to invite someone to dinner; to be the host\n";
        string += "昨天,zuótiān - yesterday\n";
        string += "所以,suǒyǐ - so\n";
        string += "好久,hǎojiǔ - a long time\n";
        string += "不错,búcuò - not bad; pretty good\n";
        string += "想,xiǎng - to want to; to think; to miss\n";
        string += "觉得,juéde - to feel/think that...\n";
        string += "有意思,yǒuyìsi - interesting\n";
        string += "只,zhǐ - only\n";
        string += "睡觉,shuìjiào - to sleep\n";
        string += "算了,suàn le - forget it; never mind\n";
        string += "找,zhǎo - to look for\n";
        string += "别人,biérén - others; other people; another person\n";
        string += "呀,ya - (an injectory particle used to soften a question)\n";
        string += "进,jìn - to enter\n";
        string += "快,kuài - fast; quick; quickly\n";
        string += "进来,jìnlai - to come in\n";
        string += "来,lái - to come\n";
        string += "介绍,jièshào - to introduce\n";
        string += "一下,yíxià - (a measure word used after a verb indicating short duration)\n";
        string += "高兴,gāoxìng - happy; pleased\n";
        string += "漂亮,piàoliang - pretty\n";
        string += "坐,zuò - to sit\n";
        string += "在,zài - at; in; on\n";
        string += "哪儿,nǎr - where\n";
        string += "工作,gōngzuò - to work; work; job\n";
        string += "学校,xuéxiào - school\n";
        string += "喝,hē - to drink\n";
        string += "点儿,diǎn(r) - a little; a bit; some\n";
        string += "茶,chá - tea\n";
        string += "咖啡,kāfēi - coffee\n";
        string += "啤酒,píjiǔ - beer\n";
        string += "吧,ba - (a suggestion particle; softens the tone of the sentence to which it is appended)\n";
        string += "要,yào - to want; to have a desire for\n";
        string += "杯,bēi - cup; glass$ measure word\n";
        string += "可乐,kělè - cola\n";
        string += "可以,kěyǐ - can; may\n";
        string += "对不起,duìbuqǐ - I'm sorry.\n";
        string += "给,gěi - to give\n";
        string += "水,shuǐ - water\n";
        string += "完儿,wán(r) - to have fun; to play\n";
        string += "图书馆,túshūguǎn - Library\n";
        string += "瓶,píng - bottle (measure word)\n";
        string += "一起,yìqǐ - together\n";
        string += "聊天,liáotiān(r) - to chat\n";
        string += "才,cái - not until$ only then\n";
        string += "回家,huíjiā - to go home\n";
        string += "帮忙,bāng máng - to help; to do someone a favor\n";
        string += "别客气,bié kèqi - don't be so polite!\n";
        string += "下个星期,xiàge xīngqī - Next week\n";
        string += "中文,Zhōngwén - the Chinese language\n";
        string += "帮,bāng - to help\n";
        string += "练习,liànxí - to practice\n";
        string += "说,shuō - to say; to speak\n";
        string += "啊,a - (a sentence–final particle of exclamation$ interrogation)\n";
        string += "但是,dànshì - but\n";
        string += "得,děi - must; to have to\n";
        string += "知道,zhīdao - to know\n";
        string += "回来,huílai - to come back\n";
        string += "跟,gēn - and\n";
        string += "说话,shuō huà - to talk (vo)\n";
        string += "上个星期,shàngge xīngqī last week\n";
        string += "得,de - a structural particle\n";
        string += "帮助,bāngzhù - to help\n";
        string += "# 复习,fùxí - to review\n";
        string += "字,zì - character\n";
        string += "写,xiě - to write\n";
        string += "慢,màn - slow\n";
        string += "教,jiāo - to teach\n";
        string += "怎么,zěnme - how\n";
        string += "就,jiù - (ADV indicates that something takes place sooner than expected)\n";
        string += "学,xué - to study\n";
        string += "笔,bǐ - pen\n";
        string += "难,nán - difficult\n";
        string += "快,kuài - quick$ fast\n";
        string += "哪里,nǎli - you flatter me. Not at all. (A polite reply to a compliment).\n";
        string += "第,dì - (a prefix for ordinal numbers)\n";
        string += "# 预习,yùxí - to preview\n";
        string += "语法,yǔfǎ - grammar\n";
        string += "容易,róngyì - easy\n";
        string += "多,duō - many; much\n";
        string += "懂,dǒng - to understand\n";
        string += "生词,shēngcí - new words\n";
        string += "汉字,hànzì - Chinese Character\n";
        string += "有一点儿,yǒu yìdiǎnr  - a little; somewhat\n";
        string += "不谢,búxiè - Don't mention it. Not at all$ you're welcstring += \n";
        return string;
    }


    public static String getChineseTop100() {
        String string = "";
        string += " people , 民 . mín\n";
        string += " time , 时间 . Shíjiān\n";
        string += " year , 一年 . yī nián\n";
        string += " thing , 事 . shì\n";
        string += " way , 道路 . dàolù\n";
        string += " day , 一天 . yītiān\n";
        string += " man , 男人 . nánrén\n";
        string += " state , 状态 . zhuàngtài\n";
        string += " lot , 地段 . dìduàn\n";
        string += "  woman , 女人 . nǚrén\n";
        string += "  country , 国内 . guónèi\n";
        string += "  life , 生活 . shēnghuó\n";
        string += "  question , 问题 . wèntí\n";
        string += "  president , 总统 . zǒngtǒng\n";
        string += "  child , 孩子 . háizi\n";
        string += "  week , 一周 . yīzhōu\n";
        string += "  world , 世界 . shìjiè\n";
        string += "  kind , 那种 . nà zhǒng\n";
        string += "  case , 案件 . ànjiàn\n";
        string += "  problem , 问题 . wèntí\n";
        string += "  fact , 事实 . shìshí\n";
        string += "  family , 家庭 . jiātíng\n";
        string += "  story , 这个故事 . zhège gùshì\n";
        string += "  point , 点 . diǎn\n";
        string += "  government , 政府 . zhèngfǔ\n";
        string += "  money , 钱 . qián\n";
        string += "  issue , 问题 . wèntí\n";
        string += "  part , 部分 . bùfèn\n";
        string += "  morning , 清晨 . qīngchén\n";
        string += "  guy , 那家伙 . nà jiāhuo\n";
        string += "  number , 数量 . shùliàng\n";
        string += "  job , 就业 . jiùyè\n";
        string += "  night , 夜晚 . yèwǎn\n";
        string += "  right , 正确的 . zhèngquè de\n";
        string += "  school , 学校 . xuéxiào\n";
        string += "  place , 的地方 . dì dìfāng\n";
        string += "  war , 战争 . zhànzhēng\n";
        string += "  kid , 孩子 . háizi\n";
        string += "  month , 一个月 . yīgè yuè\n";
        string += "  book , 本书 . běn shū\n";
        string += "  hour , 小时 . xiǎoshí\n";
        string += "  show , 演出 . yǎnchū\n";
        string += "  police , 警方 . jǐngfāng\n";
        string += "  program , 该程序 . gāi chéngxù\n";
        string += "  party , 党 . dǎng\n";
        string += "  law , 法律 . fǎlǜ\n";
        string += "  word , 字 . zì\n";
        string += "  person , 此人 . cǐ rén\n";
        string += "  home , 主场 . zhǔchǎng\n";
        string += "  business , 业务 . yèwù\n";
        string += "  friend , 朋友 . péngyǒu\n";
        string += "  company , 公司 . gōngsī\n";
        string += "  side , 侧面 . cèmiàn\n";
        string += "  tax , 税 . shuì\n";
        string += "  democrat , 民主党 . mínzhǔdǎng\n";
        string += "  group , 该集团 . gāi jítuán\n";
        string += "  moment , 瞬间 . shùnjiān\n";
        string += "  break , 破 . pò\n";
        string += "  idea , 这个想法 . zhège xiǎngfǎ\n";
        string += "  member , 成员 . chéngyuán\n";
        string += "  system , 系统 . xìtǒng\n";
        string += "  minute , 一分钟 . yī fēnzhōng\n";
        string += "  reason , 原因 . yuányīn\n";
        string += "  campaign , 竞选 . jìngxuǎn\n";
        string += "  work , 工作 . gōngzuò\n";
        string += "  nation , 全国 . quánguó\n";
        string += "  congress , 大会 . dàhuì\n";
        string += "  mother , 母亲 . mǔqīn\n";
        string += "  force , 力 . lì\n";
        string += "  health , 健康 . jiànkāng\n";
        string += "  back , 背部 . bèibù\n";
        string += "  area , 区域 . qūyù\n";
        string += "  name , 名字 . míngzì\n";
        string += "  report , 报告 . bàogào\n";
        string += "  hand , 手 . shǒu\n";
        string += "  line , 行 . xíng\n";
        string += "  end , 结束 . jiéshù\n";
        string += "  court , 法院 . fǎyuàn\n";
        string += "  care , 护理 . hùlǐ\n";
        string += "  couple , 夫妻 . fūqī\n";
        string += "  house , 房子 . fángzi\n";
        string += "  american , 美国 . měiguó\n";
        string += "  father , 父亲 . fùqīn\n";
        string += "  situation , 情况 . qíngkuàng\n";
        string += "  car , 汽车 . qìchē\n";
        string += "  parent , 父 . fù\n";
        string += "  administration , 管理 . guǎnlǐ\n";
        string += "  drug , 药物 . yàowù\n";
        string += "  city , 城市 . chéngshì\n";
        string += "  sense , 感 . gǎn\n";
        string += "  policy , 政策 . zhèngcè\n";
        string += "  office , 办公室 . bàngōngshì\n";
        string += "  decision , 决定 . juédìng\n";
        string += "  power , 动力 . dònglì\n";
        string += "  plan , 计划 . jìhuà\n";
        string += "  leader , 领导者 . lǐngdǎo zhě\n";
        string += "  information , 信息 . xìnxī\n";
        string += "  election , 选 . xuǎn\n";
        string += "  movie , 电影 . diànyǐng\n";
        string += "  death , 死亡 . sǐwáng\n";
        return string;
    }
}

//
//        101. air – 空气 . kōngqì
//        102. call – 通话 . tōnghuà
//        103. community – 社区 . shèqū
//        104. look – 外观 . wàiguān
//        105. evidence – 证据 . zhèngjù
//        106. girl – 女孩 . nǚhái
//        107. doctor – 医生 . yīshēng
//        108. vote – 投票 . tóupiào
//        109. video – 视频 . shìpín
//        110. picture – 图片 . túpiàn
//        111. room – 房间 . fángjiān
//        112. bit – 该位 . gāi wèi
//        113. economy – 经济 . jīngjì
//        114. phone – 手机 . shǒujī
//        115. history – 历史 . lìshǐ
//        116. sort – 排序 . páixù
//        117. boy – 男孩 . nánhái
//        118. crime – 犯罪 . fànzuì
//        119. process – 流程 . liúchéng
//        120. wife – 妻子 . qīzi
//        121. music – 音乐 . yīnyuè
//        122. water – 水 . shuǐ
//        123. service – 服务 . fúwù
//        124. record – 记录 . jìlù
//        125. security – 安全 . ānquán
//        126. talk – 会谈 . huìtán
//        127. mind – 心灵 . xīnlíng
//        128. head – 头部 . tóu bù
//        129. candidate – 候选 . hòuxuǎn
//        130. official – 官方 . guānfāng
//        131. camera – 摄像头 . shèxiàngtóu
//        132. student – 学生 . xuéshēng
//        133. attack – 攻击 . gōngjí
//        134. chance – 机会 . jīhuì
//        135. game – 游戏 . yóuxì
//        136. baby – 宝宝 . bǎobǎo
//        137. south – 南方 . nánfāng
//        138. change – 变化 . biànhuà
//        139. trial – 试用 . shìyòng
//        140. body – 身体 . shēntǐ
//        141. market – 市场 . shìchǎng
//        142. stuff – 东东 . dōng dōng
//        143. interest – 兴趣 . xìngqù
//        144. weapon – 武器 . wǔqì
//        145. heart – 的心脏 . de xīnzàng
//        146. son – 儿子 . érzi
//        147. team – 球队 . qiú duì
//        148. town – 小镇 . xiǎo zhèn
//        149. television – 电视 . diànshì
//        150. will – 意志 . yìzhì
//        151. troop – 队伍 . duìwǔ
//        152. judge – 法官 . fǎguān
//        153. action – 行动 . xíngdòng
//        154. answer – 答案 . dá'àn
//        155. position – 位置 . wèizhì
//        156. view – 视图 . shìtú
//        157. difference – 区别 . qūbié
//        158. food – 食品 . shípǐn
//        159. daughter – 女儿 . nǚ'ér
//        160. race – 比赛 . bǐsài
//        161. deal – 成交 . chéngjiāo
//        162. street – 街头 . jiētóu
//        163. price – 价格 . jiàgé
//        164. message – 消息 . xiāoxī
//        165. control – 控制 . kòngzhì
//        166. debate – 辩论 . biànlùn
//        167. officer – 军官 . jūnguān
//        168. film – 电影 . diànyǐng
//        169. level – 水平 . shuǐpíng
//        170. effort – 努力 . nǔlì
//        171. relationship – 关系 . guānxì
//        172. correspondent – 记者 . jìzhě
//        173. ground – 地面 . dìmiàn
//        174. piece – 片 . piàn
//        175. husband – 丈夫 . zhàngfū
//        176. budget – 预算 . yùsuàn
//        177. jury – 陪审团 . péishěn tuán
//        178. reporter – 记者 . jìzhě
//        179. weekend – 周末 . zhōumò
//        180. matter – 此事 . cǐ shì
//        181. age – 年龄 . niánlíng
//        182. gun – 枪 . qiāng
//        183. peace – 和平 . hépíng
//        184. murder – 谋杀 . móushā
//        185. role – 角色 . juésè
//        186. investigation – 调查 . diàochá
//        187. experience – 经验 . jīngyàn
//        188. hospital – 医院 . yīyuàn
//        189. eye – 眼 . yǎn
//        190. director – 导演 . dǎoyǎn
//        191. future – 未来 . wèilái
//        192. face – 面对 . miàn duì
//        193. oil – 油 . yóu
//        194. result – 结果 . jiéguǒ
//        195. public – 公众 . gōngzhòng
//        196. lawyer – 律师 . lǜshī
//        197. poll – 民意调查 . mínyì diàochá
//        198. second – 第二 . dì èr
//        199. opportunity – 机会 . jīhuì
//        200. industry – 行业 . hángyè
//        201. brother – 的哥 . dí gē
//        202. statement – 声明 . shēngmíng
//        203. fire – 火 . huǒ
//        204. love – 爱情 . àiqíng
//        205. rate – 率 . lǜ
//        206. effect – 效果 . xiàoguǒ
//        207. song – 这首歌 . zhè shǒu gē
//        208. event – 事件 . shìjiàn
//        209. charge – 充电 . chōngdiàn
//        210. support – 支持 . zhīchí
//        211. radio – 无线电 . wúxiàndiàn
//        212. building – 建筑 . jiànzhú
//        213. bank – 银行 . yínháng
//        214. victim – 受害者 . shòuhài zhě
//        215. door – 门 . mén
//        216. interview – 采访 . cǎifǎng
//        217. scene – 现场 . xiànchǎng
//        218. rest – 其余的部分 . qíyú de bùfèn
//        219. press – 新闻 . xīnwén
//        220. attention – 注意 . zhùyì
//        221. choice – 选择 . xuǎnzé
//        222. speech – 讲话 . jiǎnghuà
//        223. patient – 病人 . bìngrén
//        224. truth – 真相 . zhēnxiàng
//        225. road – 在路上 . zài lùshàng
//        226. term – 期限 . qíxiàn
//        227. site – 网站 . wǎngzhàn
//        228. worker – 工人 . gōngrén
//        229. professor – 教授 . jiàoshòu
//        230. letter – 信 . xìn
//        231. reform – 改革 . gǎigé
//        232. concern – 关注 . guānzhù
//        233. folk – 民间 . mínjiān
//        234. sex – 性别 . xìngbié
//        235. study – 研究 . yánjiū
//        236. college – 学院 . xuéyuàn
//        237. voter – 选民 . xuǎnmín
//        238. wall – 墙上 . qiáng shàng
//        239. evening – 晚上 . wǎnshàng
//        240. meeting – 会议 . huìyì
//        241. voice – 声音 . shēngyīn
//        242. audience – 观众 . guānzhòng
//        243. violence – 暴力 . bàolì
//        244. rule – 规则 . guīzé
//        245. star – 明星 . míngxīng
//        246. test – 测试 . cèshì
//        247. education – 教育 . jiàoyù
//        248. trouble – 麻烦 . máfan
//        249. risk – 风险 . fēngxiǎn
//        250. prison – 监狱 . jiānyù
//        251. guest – 客人 . kèrén
//        252. class – 类 . lèi
//        253. society – 社会 . shèhuì
//        254. soldier – 士兵 . shìbīng
//        255. plane – 飞机 . fēijī
//        256. amount – 量 . liàng
//        257. help – 帮助 . bāngzhù
//        258. cost – 成本 . chéngběn
//        259. authority – 权威 . quánwēi
//        260. foot – 脚 . jiǎo
//        261. period – 期 . qī
//        262. feeling – 感觉 . gǎnjué
//        263. step – 步骤 . bùzhòu
//        264. cause – 事业 . shìyè
//        265. tape – 磁带 . cídài
//        266. threat – 威胁 . wēixié
//        267. opinion – 意见 . yìjiàn
//        268. organization – 该组织 . gāi zǔzhī
//        269. type – 类型 . lèixíng
//        270. conversation – 谈话 . tánhuà
//        271. disease – 本病 . běn bìng
//        272. operation – 操作 . cāozuò
//        273. cancer – 癌症 . áizhèng
//        274. magazine – 该杂志 . gāi zázhì
//        275. governor – 州长 . zhōuzhǎng
//        276. pressure – 压力 . yālì
//        277. majority – 多数 . duōshù
//        278. research – 研究 . yánjiū
//        279. character – 字符 . zìfú
//        280. station – 该站 . gāi zhàn
//        281. crisis – 危机 . wéijī
//        282. expert – 专家 . zhuānjiā
//        283. blood – 血 . xuè
//        284. chairman – 董事长 . dǒngshì zhǎng
//        285. argument – 参数 . cānshù
//        286. list – 名单 . míngdān
//        287. dog – 狗 . gǒu
//        288. computer – 计算机 . jìsuànjī
//        289. beginning – 开始 . kāishǐ
//        290. insurance – 保险 . bǎoxiǎn
//        291. field – 现场 . xiànchǎng
//        292. sister – 姐姐 . jiějiě
//        293. mistake – 错误 . cuòwù
//        294. player – 播放器 . bòfàng qì
//        295. energy – 能源 . néngyuán
//        296. paper – 纸 . zhǐ
//        297. space – 空间 . kōngjiān
//        298. store – 店 . diàn
//        299. agreement – 协议 . xiéyì
//        300. responsibility – 责任 . zérèn
//        301. lady – 女士 . nǚshì
//        302. order – 顺序 . shùnxù
//        303. summer – 夏 . xià
//        304. product – 产品 . chǎnpǐn
//        305. value – 值 . zhí
//        306. stock – 股票 . gǔpiào
//        307. intelligence – 情报 . qíngbào
//        308. teacher – 老师 . lǎoshī
//        309. agency – 该机构 . gāi jī gòu
//        310. trade – 贸易 . màoyì
//        311. conference – 会议 . huìyì
//        312. cell – 细胞 . xìbāo
//        313. church – 教堂 . jiàotáng
//        314. comment – 评论 . pínglùn
//        315. source – 来源 . láiyuán
//        316. past – 过去 . guòqù
//        317. cut – 切 . qiè
//        318. thought – 思想 . sīxiǎng
//        319. credit – 信贷 . xìndài
//        320. hope – 希望 . xīwàng
//        321. sign – 符号 . fúhào
//        322. career – 职业生涯 . zhíyè shēngyá
//        323. marriage – 结婚 . jiéhūn
//        324. condition – 条件 . tiáojiàn
//        325. witness – 证人 . zhèngrén
//        326. technology – 技术 . jìshù
//        327. stage – 舞台 . wǔtái
//        328. staff – 工作人员 . gōngzuò rényuán
//        329. arm – 手臂 . shǒubì
//        330. mission – 使命 . shǐmìng
//        331. art – 艺术 . yìshù
//        332. actor – 演员 . yǎnyuán
//        333. battle – 战斗 . zhàndòu
//        334. top – 顶 . dǐng
//        335. citizen – 公民 . gōngmín
//        336. author – 笔者 . bǐzhě
//        337. reaction – 反应 . fǎnyìng
//        338. gentleman – 绅士 . shēnshì
//        339. leadership – 领导 . lǐngdǎo
//        340. fear – 恐惧 . kǒngjù
//        341. example – 这个例子 . zhège lìzi
//        342. language – 语言 . yǔyán
//        343. need – 需要 . xūyào
//        344. justice – 司法 . sīfǎ
//        345. discussion – 讨论 . tǎolùn
//        346. border – 边境 . biānjìng
//        347. animal – 动物 . dòngwù
//        348. detail – 细节 . xìjié
//        349. response – 响应 . xiǎngyìng
//        350. network – 网络 . wǎngluò
//        351. movement – 运动 . yùndòng
//        352. use – 使用 . shǐyòng
//        353. male – 男 . nán
//        354. capital – 资本 . zīběn
//        355. hearing – 听证会 . tīngzhèng huì
//        356. standard – 标准 . biāozhǔn
//        357. benefit – 受益 . shòuyì
//        358. table – 表 . biǎo
//        359. treatment – 治疗 . zhìliáo
//        360. agent – 代理 . dàilǐ
//        361. card – 该卡 . gāi kǎ
//        362. shot – 拍摄 . pāishè
//        363. board – 董事会 . dǒngshìhuì
//        364. reality – 现实 . xiànshí
//        365. sir – 该先生 . gāi xiānshēng
//        366. land – 土地 . tǔdì
//        367. project – 该项目 . gāi xiàngmù
//        368. light – 光 . guāng
//        369. image – 图像 . túxiàng
//        370. century – 世纪 . shìjì
//        371. page – 页面 . yèmiàn
//        372. success – 成功 . chénggōng
//        373. sound – 声音 . shēngyīn
//        374. impact – 影响 . yǐngxiǎng
//        375. pain – 痛 . tòng
//        376. season – 本赛季 . běn sàijì
//        377. individual – 个人 . gèrén
//        378. sport – 体育 . tǐyù
//        379. studio – 工作室 . gōngzuò shì
//        380. decade – 十年 . shí nián
//        381. challenge – 挑战 . tiǎozhàn
//        382. dream – 梦想 . mèngxiǎng
//        383. newspaper – 报纸 . bàozhǐ
//        384. floor – 地板 . dìbǎn
//        385. fight – 战斗 . zhàndòu
//        386. safety – 安全 . ānquán
//        387. act – 行为 . xíngwéi
//        388. trip – 行程 . xíngchéng
//        389. course – 课程 . kèchéng
//        390. possibility – 可能性 . kěnéng xìng
//        391. freedom – 自由 . zìyóu
//        392. fund – 基金 . jījīn
//        393. seat – 座位 . zuòwèi
//        394. bomb – 炸弹 . zhàdàn
//        395. form – 表单 . biǎodān
//        396. executive – 执行 . zhíxíng
//        397. minister – 部长 . bùzhǎng
//        398. deficit – 赤字 . chìzì
//        399. development – 发展 . fāzhǎn
//        400. flight – 飞行 . fēixíng
//        401. secretary – 秘书 . mìshū
//        402. committee – 委员会 . wěiyuánhuì
//        403. consumer – 消费者 . xiāofèi zhě
//        404. rock – 摇滚 . yáogǔn
//        405. figure – 图 . tú
//        406. subject – 主题 . zhǔtí
//        407. culture – 文化 . wénhuà
//        408. hair – 头发 . tóufǎ
//        409. coverage – 覆盖范围 . fùgài fànwéi
//        410. loss – 损失 . sǔnshī
//        411. dad – 爸爸 . bàba
//        412. note – 说明 . shuōmíng
//        413. activity – 活动 . huódòng
//        414. goal – 目标 . mùbiāo
//        415. option – 选项 . xuǎnxiàng
//        416. gas – 气 . qì
//        417. region – 该地区 . gāi dìqū
//        418. population – 人口 . rénkǒu
//        419. ability – 能力 . nénglì
//        420. resolution – 分辨率 . fēnbiàn lǜ
//        421. employee – 员工 . yuángōng
//        422. brain – 大脑 . dànǎo
//        423. politician – 政治家 . zhèngzhì jiā
//        424. middle – 中间 . zhōngjiān
//        425. storm – 风暴 . fēngbào
//        426. generation – 发电 . fādiàn
//        427. critic – 评论家 . pínglùn jiā
//        428. fan – 风扇 . fēngshàn
//        429. scientist – 科学家 . kēxuéjiā
//        430. target – 目标 . mùbiāo
//        431. camp – 营地 . yíngdì
//        432. afternoon – 下午 . xiàwǔ
//        433. direction – 方向 . fāngxiàng
//        434. science – 科学 . kēxué
//        435. journalist – 记者 . jìzhě
//        436. degree – 度 . dù
//        437. affair – 外遇 . wàiyù
//        438. damage – 伤害 . shānghài
//        439. strategy – 战略 . zhànlüè
//        440. factor – 因素 . yīnsù
//        441. plant – 工厂 . gōngchǎng
//        442. democracy – 民主 . mínzhǔ
//        443. doubt – 疑点 . yídiǎn
//        444. solution – 该解决方案 . gāi jiějué fāng'àn
//        445. spending – 消费 . xiāofèi
//        446. training – 培训 . péixùn
//        447. abuse – 滥用 . lànyòng
//        448. danger – 危险 . wéixiǎn
//        449. environment – 环境 . huánjìng
//        450. memory – 记忆 . jìyì
//        451. nature – 大自然 . dà zìrán
//        452. material – 材料 . cáiliào
//        453. document – 文件 . wénjiàn
//        454. income – 收入 . shōurù
//        455. analyst – 分析师 . fēnxī shī
//        456. play – 播放 . bòfàng
//        457. guard – 后卫 . hòuwèi
//        458. hotel – 酒店 . jiǔdiàn
//        459. editor – 编辑 . biānjí
//        460. missile – 导弹 . dǎodàn
//        461. accident – 事故 . shìgù
//        462. purpose – 目的 . mùdì
//        463. writer – 作家 . zuòjiā
//        464. emergency – 紧急 . jǐn jí
//        465. growth – 成长 . chéngzhǎng
//        466. bed – 床上 . chuángshàng
//        467. welfare – 福利 . fúlì
//        468. adult – 成人 . chéngrén
//        469. faith – 信念 . xìnniàn
//        470. proposal – 提案 . tí'àn
//        471. model – 该模型 . gāi móxíng
//        472. army – 军队 . jūnduì
//        473. incident – 事件 . shìjiàn
//        474. box – 包装盒 . bāozhuāng hé
//        475. crowd – 人群 . rénqún
//        476. respect – 尊重 . zūnzhòng
//        477. department – 部门 . bùmén
//        478. access – 访问 . fǎngwèn
//        479. date – 日期 . rìqí
//        480. chief – 首席 . shǒuxí
//        481. weather – 天气 . tiānqì
//        482. airport – 机场 . jīchǎng
//        483. machine – 本机 . běn jī
//        484. window – 窗口 . chuāngkǒu
//        485. agenda – 议程 . yìchéng
//        486. advice – 建议 . jiànyì
//        487. prosecution – 起诉 . qǐsù
//        488. conflict – 冲突 . chōngtú
//        489. fall – 秋季 . qiūjì
//        490. pilot – 试点 . shìdiǎn
//        491. practice – 实践 . shíjiàn
//        492. track – 轨道 . guǐdào
//        493. owner – 店主 . diànzhǔ
//        494. increase – 增加 . zēngjiā
//        495. legislation – 立法 . lìfǎ
//        496. tree – 树 . shù
//        497. debt – 债务 . zhàiwù
//        498. enemy – 敌人 . dírén
//        499. truck – 卡车 . kǎchē
//        500. lead – 领先 . lǐngxiān
//        501. convention – 公约 . gōngyuē
//        502. aid – 援助 . yuánzhù
//        503. suicide – 自杀 . zìshā
//        504. quality – 质量 . zhìliàng
//        505. conservative – 保守 . bǎoshǒu
//        506. size – 尺寸 . chǐcùn
//        507. account – 帐户 . zhànghù
//        508. pleasure – 乐趣 . lèqù
//        509. institution – 该机构 . gāi jīgòu
//        510. airline – 航空公司 . hángkōng gōngsī
//        511. weight – 重量 . zhòngliàng
//        512. approach – 该方法 . gāi fāngfǎ
//        513. surgery – 手术 . shǒushù
//        514. holiday – 节日 . jiérì
//        515. start – 开始 . kāishǐ
//        516. victory – 胜利 . shènglì
//        517. district – 区 . qū
//        518. relation – 关系 . guānxì
//        519. search – 搜索 . sōusuǒ
//        520. crew – 船员 . chuányuán
//        521. panel – 面板 . miànbǎn
//        522. coalition – 联盟 . liánméng
//        523. move – 举 . jǔ
//        524. package – 包 . bāo
//        525. check – 检查 . jiǎnchá
//        526. minority – 少数 . shǎoshù
//        527. unit – 单位 . dānwèi
//        528. bag – 包 . bāo
//        529. surprise – 惊喜 . jīngxǐ
//        530. focus – 重点 . zhòngdiǎn
//        531. circumstance – 的情况下 . de qíngkuàng xià
//        532. producer – 生产者 . shēngchǎn zhě
//        533. investment – 投资 . tóuzī
//        534. ball – 球 . qiú
//        535. colleague – 同事 . tóngshì
//        536. resource – 资源 . zīyuán
//        537. judgment – 判决 . pànjué
//        538. background – 后台 . hòutái
//        539. union – 工会 . gōnghuì
//        540. address – 地址 . dìzhǐ
//        541. driver – 司机 . sījī
//        542. hero – 英雄 . yīngxióng
//        543. medicine – 药 . yào
//        544. representative – 代表 . dàibiǎo
//        545. vehicle – 汽车 . qìchē
//        546. commander – 指挥官 . zhǐhuī guān
//        547. artist – 艺术家 . yìshùjiā
//        548. mayor – 市长 . shì zhǎng
//        549. run – 运行 . yùnxíng
//        550. ship – 船 . chuán
//        551. theory – 理论 . lǐlùn
//        552. screen – 屏幕 . píngmù
//        553. lesson – 教训 . jiàoxùn
//        554. resident – 驻地 . zhùdì
//        555. partner – 合作伙伴 . hézuò huǒbàn
//        556. bar – 酒吧 . jiǔbā
//        557. ticket – 门票 . ménpiào
//        558. disaster – 灾难 . zāinàn
//        559. performance – 性能 . xìngnéng
//        560. supporter – 的支持者 . de zhīchí zhě
//        561. killer – 杀手 . shāshǒu
//        562. negotiation – 谈判 . tánpàn
//        563. sea – 海 . hǎi
//        564. wind – 风 . fēng
//        565. shoe – 鞋 . xié
//        566. secret – 秘密 . mìmì
//        567. tragedy – 悲剧 . bēijù
//        568. gift – 礼物 . lǐwù
//        569. tour – 游 . yóu
//        570. manager – 经理 . jīnglǐ
//        571. band – 乐队 . yuèduì
//        572. birth – 诞生 . dànshēng
//        573. host – 主人 . zhǔrén
//        574. loan – 贷款 . dàikuǎn
//        575. allegation – 指控 . zhǐkòng
//        576. winner – 赢家 . yíngjiā
//        577. religion – 宗教 . zōngjiào
//        578. article – 文章 . wénzhāng
//        579. session – 会议 . huìyì
//        580. being – 在幸福 . zài xìngfú
//        581. extent – 范围 . fànwéi
//        582. contract – 合同 . hétóng
//        583. sentence – 句子 . jùzi
//        584. measure – 措施 . cuòshī
//        585. deputy – 副 . fù
//        586. confidence – 信心 . xìnxīn
//        587. hell – 地狱 . dìyù
//        588. relief – 救援 . jiùyuán
//        589. perspective – 透视 . tòushì
//        590. advantage – 优势 . yōushì
//        591. scandal – 丑闻 . chǒuwén
//        592. leg – 腿 . tuǐ
//        593. restaurant – 餐厅 . cāntīng
//        594. dinner – 晚餐 . wǎncān
//        595. client – 客户端 . kèhù duān
//        596. failure – 失败 . shībài
//        597. quarter – 本季度 . běn jìdù
//        598. element – 元素 . yuánsù
//        599. claim – 索赔 . suǒpéi
//        600. property – 物业 . wùyè
//        601. version – 该版本 . gāi bǎnběn
//        602. attempt – 尝试 . chángshì
//        603. bus – 公交车 . gōngjiāo chē
//        604. customer – 客户 . kèhù
//        605. appeal – 上诉 . shàngsù
//        606. pound – 英镑 . yīngbàng
//        607. protection – 保护 . bǎohù
//        608. gene – 基因 . jīyīn
//        609. stone – 石 . shí
//        610. opposition – 反对派 . fǎnduì pài
//        611. apartment – 公寓 . gōngyù
//        612. front – 前 . qián
//        613. female – 女性 . nǚxìng
//        614. prisoner – 囚犯 . qiúfàn
//        615. black – 黑 . hēi
//        616. gang – 帮派 . bāngpài
//        617. principle – 原则 . yuánzé
//        618. ally – 盟友 . méngyǒu
//        619. adviser – 该顾问 . gāi gùwèn
//        620. visit – 访问 . fǎngwèn
//        621. bird – 鸟 . niǎo
//        622. stand – 展台 . zhǎntái
//        623. communication – 通信 . tōngxìn
//        624. boat – 船 . chuán
//        625. connection – 连接 . liánjiē
//        626. key – 关键 . guānjiàn
//        627. strike – 罢工 . bàgōng
//        628. penalty – 点球 . diǎn qiú
//        629. warning – 警示 . jǐngshì
//        630. luck – 运气 . yùnqì
//        631. dna – 该DNA . gāi DNA
//        632. spirit – 精神 . jīngshén
//        633. turn – 转 . zhuǎn
//        634. facility – 设施 . shèshī
//        635. teenager – 小将 . xiǎojiàng
//        636. commitment – 承诺 . chéngnuò
//        637. item – 该项目 . gāi xiàngmù
//        638. procedure – 步骤 . bùzhòu
//        639. half – 半 . bàn
//        640. tank – 坦克 . tǎnkè
//        641. telephone – 电话 . diànhuà
//        642. supply – 供应 . gōngyìng
//        643. attitude – 态度 . tàidù
//        644. block – 块 . kuài
//        645. bond – 债券 . zhàiquàn
//        646. style – 风格 . fēnggé
//        647. football – 足球 . zúqiú
//        648. fuel – 燃料 . ránliào
//        649. data – 数据 . shùjù
//        650. glass – 玻璃 . bōlí
//        651. suit – 西装 . xīzhuāng
//        652. strength – 实力 . shílì
//        653. criticism – 批评 . pīpíng
//        654. ice – 冰 . bīng
//        655. equipment – 设备 . shèbèi
//        656. promise – 承诺 . chéng nuò
//        657. return – 回报 . huíbào
//        658. album – 这张专辑 . zhè zhāng zhuānjí
//        659. production – 生产 . shēngchǎn
//        660. corner – 角落 . jiǎoluò
//        661. consequence – 后果 . hòuguǒ
//        662. living – 客厅 . kètīng
//        663. injury – 受伤 . shòushāng
//        664. cover – 封面 . fēngmiàn
//        665. spot – 现货 . xiànhuò
//        666. breast – 乳房 . rǔfáng
//        667. spring – 春天 . chūntiān
//        668. controversy – 争议 . zhēngyì
//        669. destruction – 毁灭 . huǐmiè
//        670. aspect – 纵横 . zònghéng
//        671. club – 俱乐部 . jùlèbù
//        672. aircraft – 飞机 . fēijī
//        673. firm – 该公司 . gāi gōngsī
//        674. regime – 政权 . zhèngquán
//        675. competition – 竞争 . jìngzhēng
//        676. profit – 利润 . lìrùn
//        677. knowledge – 知识 . zhīshì
//        678. spokesman – 发言人 . fāyán rén
//        679. review – 审查 . shěnchá
//        680. understanding – 了解 . liǎojiě
//        681. recession – 经济衰退 . jīngjì shuāituì
//        682. singer – 歌手 . gēshǒu
//        683. village – 村 . cūn
//        684. arrest – 落网 . luòwǎng
//        685. snow – 雪 . xuě
//        686. joke – 笑话 . xiàohuà
//        687. gold – 金 . jīn
//        688. contact – 联系人 . liánxì rén
//        689. demand – 需求 . xūqiú
//        690. task – 任务 . rènwù
//        691. hole – 孔 . kǒng
//        692. general – 一般 . yībān
//        693. skin – 皮肤 . pífū
//        694. count – 计数 . jìshù
//        695. opponent – 对手 . duìshǒu
//        696. conclusion – 结论 . jiélùn
//        697. priority – 优先级 . yōuxiān jí
//        698. rape – 强奸 . qiángjiān
//        699. refugee – 难民 . nànmín
//        700. mountain – 山 . shān
//        701. summit – 峰会 . fēnghuì
//        702. bottom – 底部 . dǐbù
//        703. management – 管理 . guǎnlǐ
//        704. commission – 佣金 . yōngjīn
//        705. range – 范围 . fànwéi
//        706. code – 代码 . dàimǎ
//        707. cash – 现金 . xiànjīn
//        708. mouth – 口 . kǒu
//        709. presence – 存在 . cúnzài
//        710. share – 份额 . fèn'é
//        711. fashion – 时尚 . shíshàng
//        712. shape – 形状 . xíngzhuàng
//        713. chair – 椅子 . yǐzi
//        714. finger – 手指 . shǒuzhǐ
//        715. fish – 鱼 . yú
//        716. edition – 版本 . bǎnběn
//        717. edge – 边缘 . biānyuán
//        718. limit – 极限 . jíxiàn
//        719. train – 火车 . huǒchē
//        720. belief – 信仰 . xìnyǎng
//        721. balance – 平衡 . pínghéng
//        722. exchange – 交流 . jiāoliú
//        723. farm – 农场 . nóngchǎng
//        724. zone – 区域 . qūyù
//        725. passenger – 乘客 . chéngkè
//        726. lie – 谎言 . huǎngyán
//        727. duty – 值班 . zhíbān
//        728. traffic – 交通 . jiāotōng
//        729. horse – 马 . mǎ
//        730. tradition – 传统 . chuántǒng
//        731. recovery – 恢复 . huīfù
//        732. inspector – 检查员 . jiǎnchá yuán
//        733. difficulty – 难度 . nándù
//        734. file – 文件 . wénjiàn
//        735. birthday – 生日 . shēngrì
//        736. finance – 财务 . cáiwù
//        737. crash – 崩溃 . bēngkuì
//        738. influence – 影响 . yǐngxiǎng
//        739. regulation – 监管 . jiānguǎn
//        740. analysis – 分析 . fēnxī
//        741. struggle – 斗争 . dòuzhēng
//        742. heat – 热 . rè
//        743. dress – 礼服 . lǐfú
//        744. release – 发布 . fābù
//        745. winter – 冬季 . dōngjì
//        746. virus – 该病毒 . gāi bìngdú
//        747. theme – 主题 . zhǔtí
//        748. notion – 概念 . gàiniàn
//        749. device – 设备 . shèbèi
//        750. anger – 愤怒 . fènnù
//        751. english – 英文 . yīngwén
//        752. flag – 旗 . qí
//        753. context – 上下文 . shàngxiàwén
//        754. wing – 翼 . yì
//        755. fighting – 战斗 . zhàndòu
//        756. pattern – 模式 . móshì
//        757. entertainment – 娱乐 . yúlè
//        758. appearance – 外观 . wàiguān
//        759. saving – 储蓄 . chúxù
//        760. investor – 投资者 . tóuzī zhě
//        761. bridge – 桥 . qiáo
//        762. settlement – 结算 . jiésuàn
//        763. rain – 雨 . yǔ
//        764. wood – 木 . mù
//        765. mail – 邮件 . yóujiàn
//        766. farmer – 农民 . nóngmín
//        767. mystery – 谜 . mí
//        768. prayer – 祈祷 . qídǎo
//        769. vision – 视力 . shìlì
//        770. depression – 抑郁症 . yìyù zhèng
//        771. explosion – 爆炸 . bàozhà
//        772. defendant – 被告 . bèigào
//        773. structure – 结构 . jiégòu
//        774. amendment – 修正 . xiūzhèng
//        775. assault – 攻击 . gōngjí
//        776. thinking – 思维 . sīwéi
//        777. therapy – 该疗法 . gāi liáofǎ
//        778. nurse – 护士 . hùshì
//        779. path – 路径 . lùjìng
//        780. funding – 资金 . zījīn
//        781. tool – 该工具 . gāi gōngjù
//        782. title – 称号 . chēnghào
//        783. housing – 住房 . zhùfáng
//        784. shop – 店 . diàn
//        785. complaint – 投诉 . tóusù
//        786. wave – 波 . bō
//        787. cat – 猫 . māo
//        788. map – 地图 . dìtú
//        789. employer – 雇主 . gùzhǔ
//        790. proof – 证明 . zhèngmíng
//        791. emotion – 情感 . qínggǎn
//        792. skill – 技能 . jìnéng
//        793. trust – 信任 . xìnrèn
//        794. topic – 话题 . huàtí
//        795. personality – 个性 . gèxìng
//        796. hall – 大厅 . dàtīng
//        797. cabinet – 内阁 . nèigé
//        798. bone – 骨 . gǔ
//        799. soul – 灵魂 . línghún
//        800. chicken – 鸡 . jī
//        801. location – 地点 . dìdiǎn
//        802. indication – 指示 . zhǐshì
//        803. speaker – 扬声器 . yángshēngqì
//        804. wage – 工资 . gōngzī
//        805. era – 时代 . shídài
//        806. request – 要求 . yāoqiú
//        807. boss – 老板 . lǎobǎn
//        808. coach – 教练 . jiàoliàn
//        809. estate – 地产 . dìchǎn
//        810. sector – 行业 . hángyè
//        811. egg – 鸡蛋 . jīdàn
//        812. stake – 股权 . gǔquán
//        813. stop – 停止 . tíngzhǐ
//        814. ring – 环 . huán
//        815. shock – 震荡 . zhèndàng
//        816. contribution – 贡献 . gòngxiàn
//        817. announcement – 公布 . gōngbù
//        818. cigarette – 香烟 . xiāngyān
//        819. kitchen – 厨房 . chúfáng
//        820. territory – 境内 . jìngnèi
//        821. command – 命令 . mìnglìng
//        822. island – 岛 . dǎo
//        823. protest – 抗议 . kàngyì
//        824. impression – 印象 . yìnxiàng
//        825. fault – 故障 . gùzhàng
//        826. coast – 海岸 . hǎi'àn
//        827. reading – 阅读 . yuèdú
//        828. variety – 品种 . pǐnzhǒng
//        829. revenue – 收入 . shōurù
//        830. bell – 钟 . zhōng
//        831. coffee – 咖啡 . kāfēi
//        832. alcohol – 酒精 . jiǔjīng
//        833. lunch – 午餐 . wǔcān
//        834. expectation – 预期 . yùqí
//        835. grade – 档次 . dàngcì
//        836. diet – 饮食 . yǐnshí
//        837. section – 部分 . bùfèn
//        838. ear – 耳朵 . ěrduǒ
//        839. sky – 天空 . tiānkōng
//        840. assistance – 援助 . yuánzhù
//        841. signal – 信号 . xìnhào
//        842. stress – 应力 . yìnglì
//        843. alternative – 替代 . tìdài
//        844. researcher – 研究人员 . yánjiū rényuán
//        845. cable – 电缆 . diànlǎn
//        846. opening – 开幕 . kāimù
//        847. beach – 沙滩 . shātān
//        848. volunteer – 志愿者 . zhìyuàn zhě
//        849. burden – 负担 . fùdān
//        850. detective – 侦探 . zhēntàn
//        851. toy – 玩具 . wánjù
//        852. gain – 增益 . zēngyì
//        853. asset – 资产 . zīchǎn
//        854. pool – 泳池 . yǒngchí
//        855. trend – 趋势 . qūshì
//        856. survey – 调查 . diàochá
//        857. planet – 这个星球 . zhège xīngqiú
//        858. river – 江 . jiāng
//        859. identity – 身份 . shēnfèn
//        860. ocean – 大洋 . dàyáng
//        861. joy – 喜悦 . xǐyuè
//        862. delay – 延迟 . yánchí
//        863. clinic – 诊所 . zhěnsuǒ
//        864. yard – 院子里 . yuànzi lǐ
//        865. tip – 尖端 . jiānduān
//        866. divorce – 离婚 . líhūn
//        867. sun – 太阳 . tàiyáng
//        868. row – 行 . xíng
//        869. phrase – 短语 . duǎnyǔ
//        870. worth – 万般 . wànbān
//        871. collection – 收集 . shōují
//        872. occasion – 际 . jì
//        873. surface – 表面 . biǎomiàn
//        874. tie – 领带 . lǐngdài
//        875. musician – 音乐家 . yīnyuè jiā
//        876. concept – 概念 . gàiniàn
//        877. tension – 张力 . zhānglì
//        878. outcome – 结果 . jiéguǒ
//        879. approval – 批准 . pīzhǔn
//        880. fellow – 老乡 . lǎoxiāng
//        881. beauty – 美女 . měinǚ
//        882. casualty – 伤亡 . shāngwáng
//        883. copy – 副本 . fùběn
//        884. mine – 矿井 . kuàngjǐng
//        885. watch – 手表 . shǒubiǎo
//        886. sleep – 睡眠 . shuìmián
//        887. average – 平均 . píngjūn
//        888. travel – 旅游 . lǚyóu
//        889. fraud – 欺诈 . qīzhà
//        890. category – 类别 . lèibié
//        891. exercise – 演习 . yǎnxí
//        892. conviction – 信念 . xìnniàn
//        893. initiative – 主动 . zhǔdòng
//        894. technique – 技术 . jìshù
//        895. recording – 录音 . lùyīn
//        896. sight – 视线 . shìxiàn
//        897. reduction – 减少 . jiǎnshǎo
//        898. whole – 整个 . zhěnggè
//        899. corporation – 公司 . gōngsī
//        900. passion – 激情 . jīqíng
//        901. funeral – 葬礼 . zànglǐ
//        902. youth – 青春 . qīngchūn
//        903. instrument – 仪器 . yíqì
//        904. distance – 距离 . jùlí
//        905. desert – 沙漠 . shāmò
//        906. anniversary – 周年 . zhōunián
//        907. writing – 写作 . xiězuò
//        908. invasion – 入侵 . rùqīn
//        909. compromise – 妥协 . tuǒxié
//        910. illness – 病情 . bìngqíng
//        911. tear – 泪 . lèi
//        912. gate – 门 . mén
//        913. forest – 林 . lín
//        914. experiment – 实验 . shíyàn
//        915. golf – 高尔夫 . gāo'ěrfū
//        916. talent – 人才 . réncái
//        917. post – 后 . hòu
//        918. gap – 差距 . chājù
//        919. priest – 牧师 . mùshī
//        920. novel – 小说 . xiǎoshuō
//        921. division – 师 . shī
//        922. desire – 欲望 . yùwàng
//        923. hat – 帽子 . màozi
//        924. mortgage – 抵押贷款 . dǐyā dàikuǎn
//        925. reputation – 信誉 . xìnyù
//        926. score – 比分 . bǐfēn
//        927. shoulder – 肩 . jiān
//        928. relative – 相对 . xiāngduì
//        929. column – 列 . liè
//        930. payment – 支付 . zhīfù
//        931. transition – 过渡 . guòdù
//        932. award – 获奖 . huòjiǎng
//        933. capability – 能力 . nénglì
//        934. definition – 定义 . dìngyì
//        935. pocket – 口袋 . kǒudài
//        936. speed – 速度 . sùdù
//        937. concert – 演唱会 . yǎnchàng huì
//        938. climate – 气候 . qìhòu
//        939. clue – 线索 . xiànsuǒ
//        940. beer – 啤酒 . píjiǔ
//        941. nose – 鼻子 . bízi
//        942. green – 绿 . lǜ
//        943. garden – 花园 . huāyuán
//        944. satellite – 卫星 . wèixīng
//        945. ceremony – 仪式 . yíshì
//        946. human – 人类 . rénlèi
//        947. discovery – 发现 . fāxiàn
//        948. ride – 搭 . dā
//        949. root – 根 . gēn
//        950. tea – 茶 . chá
//        951. motion – 的议案 . de yì'àn
//        952. percentage – 百分比 . bǎifēnbǐ
//        953. combination – 组合 . zǔhé
//        954. explanation – 说明 . shuōmíng
//        955. scale – 规模 . guīmó
//        956. chain – 链 . liàn
//        957. twin – 双子 . shuāngzǐ
//        958. capacity – 容量 . róngliàng
//        959. dealer – 经销商 . jīngxiāo shāng
//        960. touch – 触摸 . chùmō
//        961. journey – 旅程 . lǚchéng
//        962. delegate – 委托 . wěituō
//        963. link – 链接 . liànjiē
//        964. plastic – 塑料 . sùliào
//        965. stimulus – 刺激 . cìjī
//        966. cap – 帽 . mào
//        967. roll – 辊 . gǔn
//        968. construction – 建设 . jiànshè
//        969. intention – 意向 . yìxiàng
//        970. dispute – 争议 . zhēngyì
//        971. mood – 情绪 . qíngxù
//        972. design – 设计 . shèjì
//        973. childhood – 童年 . tóngnián
//        974. engine – 发动机 . fādòngjī
//        975. lifetime – 寿命 . shòumìng
//        976. nightmare – 噩梦 . èmèng
//        977. dance – 舞蹈 . wǔdǎo
//        978. meal – 餐 . cān
//        979. offer – 报价 . bàojià
//        980. obligation – 义务 . yìwù
//        981. advance – 前进 . qiánjìn
//        982. charity – 慈善 . císhàn
//        983. tourist – 旅游 . lǚyóu
//        984. factory – 工厂 . gōngchǎng
//        985. fat – 脂肪 . zhīfáng
//        986. salt – 盐 . yán
//        987. neck – 颈部 . jǐng bù
//        988. bedroom – 卧室 . wòshì
//        989. episode – 情节 . qíngjié
//        990. engineer – 工程师 . gōngchéngshī
//        991. punishment – 处罚 . chǔfá
//        992. testing – 测试 . cèshì
//        993. instance – 实例 . shílì
//        994. prospect – 前景 . qiánjǐng
//        995. shopping – 购物 . gòuwù
//        996. smoke – 烟 . yān
//        997. meat – 肉 . ròu
//        998. hold – 保持 . bǎochí
//        999. retirement – 退休 . tuìxiū
//        1000. inflation – 通胀 . tōngzhàng
//        1001. drama – 电视剧 . diànshìjù
//        1002. desk – 办公桌 . bàngōng zhuō
//        1003. salary – 工资 . gōngzī
//        1004. bible – 圣经 . shèngjīng
//        1005. grant – 补助 . bǔzhù
//        1006. demonstration – 示范 . shìfàn
//        1007. dialogue – 对话 . duìhuà
//        1008. substance – 物质 . wùzhí
//        1009. core – 核心 . héxīn
//        1010. treaty – 条约 . tiáoyuē
//        1011. personnel – 人员 . rényuán
//        1012. sample – 样本 . yàngběn
//        1013. tactic – 战术 . zhànshù
//        1014. method – 该方法 . gāi fāngfǎ
//        1015. noise – 噪音 . zàoyīn
//        1016. objective – 客观 . kèguān
//        1017. flower – 花 . huā
//        1018. cup – 杯 . bēi
//        1019. temperature – 温度 . wēndù
//        1020. symbol – 符号 . fúhào
//        1021. revolution – 革命 . gémìng
//        1022. taste – 味道 . wèidào
//        1023. museum – 博物馆 . bówùguǎn
//        1024. profile – 配置文件 . pèizhì wénjiàn
//        1025. suggestion – 建议 . jiànyì
//        1026. route – 路线 . lùxiàn
//        1027. tooth – 牙齿 . yáchǐ
//        1028. consultant – 顾问 . gùwèn
//        1029. drive – 该驱动器 . gāi qūdòngqì
//        1030. disorder – 无序 . wú xù
//        1031. sugar – 糖 . táng
//        1032. rise – 崛起 . juéqǐ
//        1033. chip – 该芯片 . gāi xīnpiàn
//        1034. motive – 动机 . dòngjī
//        1035. shirt – 衬衫 . chènshān
//        1036. port – 港口 . gǎngkǒu
//        1037. potential – 潜力 . qiánlì
//        1038. schedule – 时间表 . shíjiān biǎo
//        1039. parliament – 议会 . yìhuì
//        1040. contest – 竞赛 . jìngsài
//        1041. atmosphere – 氛围 . fēnwéi
//        1042. branch – 分支 . fēnzhī
//        1043. estimate – 估计 . gūjì
//        1044. symptom – 症状 . zhèngzhuàng
//        1045. involvement – 参与 . cānyù
//        1046. discrimination – 歧视 . qíshì
//        1047. heaven – 天堂 . tiāntáng
//        1048. walk – 步行 . bùxíng
//        1049. operator – 运营商 . yùnyíng shāng
//        1050. recommendation – 推荐 . tuījiàn
//        1051. rebel – 叛军 . pàn jūn
//        1052. round – 圆 . yuán
//        1053. maker – 制造商 . zhìzào shāng
//        1054. wine – 酒 . jiǔ
//        1055. remark – 此言 . cǐ yán
//        1056. mess – 残局 . cánjú
//        1057. tone – 调 . diào
//        1058. resistance – 阻力 . zǔlì
//        1059. importance – 的重要性 . de zhòngyào xìng
//        1060. exception – 除 . chú
//        1061. platform – 该平台 . gāi píngtái
//        1062. knife – 刀 . dāo
//        1063. perception – 感知 . gǎnzhī
//        1064. fee – 收费 . shōufèi
//        1065. master – 主 . zhǔ
//        1066. circle – 圆 . yuán
//        1067. manner – 的方式 . de fāngshì
//        1068. jet – 喷气机 . pēnqì jī
//        1069. meaning – 意思 . yìsi
//        1070. assessment – 评估 . pínggū
//        1071. plate – 板 . bǎn
//        1072. expense – 费用 . fèiyòng
//        1073. plot – 剧情 . jùqíng
//        1074. classroom – 课堂 . kètáng
//        1075. brand – 品牌 . pǐnpái
//        1076. painting – 绘画 . huìhuà
//        1077. roof – 屋顶 . wūdǐng
//        1078. privilege – 特权 . tèquán
//        1079. drink – 喝 . hē
//        1080. ban – 禁令 . jìnlìng
//        1081. content – 内容 . nèiróng
//        1082. chemical – 化学 . huàxué
//        1083. french – 法国 . fàguó
//        1084. inspection – 检查 . jiǎnchá
//        1085. incentive – 激励 . jīlì
//        1086. piano – 钢琴 . gāngqín
//        1087. bottle – 瓶子 . píngzi
//        1088. waste – 浪费 . làngfèi
//        1089. error – 错误 . cuòwù
//        1090. label – 标签 . biāoqiān
//        1091. clock – 时钟 . shízhōng
//        1092. cream – 霜 . shuāng
//        1093. good – 好 . hǎo
//        1094. phase – 相 . xiāng
//        1095. philosophy – 哲学 . zhéxué
//        1096. conduct – 行为 . xíngwéi
//        1097. cycle – 周期 . zhōuqí
//        1098. electricity – 电力 . diànlì
//        1099. bishop – 主教 . zhǔjiào
//        1100. reader – 读者 . dúzhě
//        1101. pregnancy – 怀孕 . huáiyùn
//        1102. league – 联赛 . liánsài
//        1103. pet – 宠物 . chǒngwù
//        1104. foundation – 基础 . jīchǔ
//        1105. boom – 繁荣 . fánróng
//        1106. tower – 塔 . tǎ
//        1107. feature – 该功能 . gāi gōngnéng
//        1108. withdrawal – 撤离 . chèlí
//        1109. breath – 呼吸 . hūxī
//        1110. crack – 裂纹 . lièwén
//        1111. manufacturer – 生产厂家 . shēngchǎn chǎngjiā
//        1112. specialist – 专科 . zhuānkē
//        1113. reference – 参考 . cānkǎo
//        1114. tale – 传说 . chuán shuō
//        1115. provision – 提供 . tígōng
//        1116. historian – 历史学家 . lìshǐ xué jiā
//        1117. wire – 电线 . diànxiàn
//        1118. speculation – 炒作 . chǎozuò
//        1119. pair – 这对 . zhè duì
//        1120. excuse – 借口 . jièkǒu
//        1121. chocolate – 巧克力 . qiǎokèlì
//        1122. celebration – 庆典 . qìngdiǎn
//        1123. cheese – 奶酪 . nǎilào
//        1124. fortune – 财富 . cáifù
//        1125. bathroom – 浴室 . yùshì
//        1126. function – 功能 . gōngnéng
//        1127. universe – 宇宙 . yǔzhòu
//        1128. channel – 通道 . tōngdào
//        1129. smile – 微笑 . wéixiào
//        1130. milk – 牛奶 . niúnǎi
//        1131. requirement – 要求 . yāoqiú
//        1132. suspicion – 怀疑 . huáiyí
//        1133. household – 家庭 . jiātíng
//        1134. finding – 这一发现 . zhè yī fà xiàn
//        1135. chest – 胸部 . xiōngbù
//        1136. routine – 常规 . chángguī
//        1137. consideration – 考虑 . kǎolǜ
//        1138. library – 图书馆 . túshū guǎn
//        1139. harm – 危害 . wéihài
//        1140. margin – 保证金 . bǎozhèngjīn
//        1141. object – 对象 . duìxiàng
//        1142. guitar – 吉他 . jítā
//        1143. button – 按钮 . ànniǔ
//        1144. silence – 沉默 . chénmò
//        1145. improvement – 改善 . gǎishàn
//        1146. drop – 下降 . xiàjiàng
//        1147. appointment – 约会 . yuēhuì
//        1148. businessman – 商人 . shāngrén
//        1149. flow – 流 . liú
//        1150. metal – 金属 . jīnshǔ
//        1151. mirror – 镜子 . jìngzi
//        1152. present – 目前 . mùqián
//        1153. carrier – 运营商 . yùnyíng shāng
//        1154. expression – 表达 . biǎodá
//        1155. fate – 命运 . mìngyùn
//        1156. cloud – 云计算 . yún jìsuàn
//        1157. muscle – 肌肉 . jīròu
//        1158. belt – 皮带 . pídài
//        1159. chapter – 章 . zhāng
//        1160. string – 字符串 . zìfú chuàn
//        1161. trading – 交易 . jiāoyì
//        1162. trick – 诀窍 . juéqiào
//        1163. guide – 指南 . zhǐnán
//        1164. volume – 音量 . yīnliàng
//        1165. match – 比赛 . bǐsài
//        1166. fantasy – 幻想 . huànxiǎng
//        1167. knee – 膝盖 . xīgài
//        1168. compound – 该化合物 . gāi huàhéwù
//        1169. stomach – 胃 . wèi
//        1170. height – 高度 . gāodù
//        1171. draft – 草案 . cǎo'àn
//        1172. stranger – 陌生人 . mòshēng rén
//        1173. infection – 感染 . gǎnrǎn
//        1174. mass – 大众 . dàzhòng
//        1175. discipline – 纪律 . jìlǜ
//        1176. fruit – 水果 . shuǐguǒ
//        1177. visitor – 游客 . yóukè
//        1178. poem – 诗 . shī
//        1179. phenomenon – 现象 . xiànxiàng
//        1180. fence – 篱笆 . líbā
//        1181. application – 应用 . yìngyòng
//        1182. recipe – 配方 . pèifāng
//        1183. clothing – 服装 . fúzhuāng
//        1184. guilt – 有罪 . yǒuzuì
//        1185. survival – 生存 . shēngcún
//        1186. champion – 冠军 . guànjūn
//        1187. uniform – 统一 . tǒngyī
//        1188. print – 打印 . dǎyìn
//        1189. potato – 马铃薯 . mǎlíngshǔ
//        1190. arrangement – 安排 . ānpái
//        1191. moon – 月亮 . yuèliàng
//        1192. bear – 熊 . xióng
//        1193. wound – 伤口 . shāngkǒu
//        1194. stability – 稳定性 . wěndìng xìng
//        1195. pride – 骄傲 . jiāo'ào
//        1196. restriction – 限制 . xiànzhì
//        1197. total – 总 . zǒng
//        1198. barrier – 障碍 . zhàng'ài
//        1199. cousin – 表姐 . biǎojiě
//        1200. occupation – 职业 . zhíyè
//        1201. silver – 银 . yín
//        1202. cake – 蛋糕 . dàngāo
//        1203. lung – 肺 . fèi
//        1204. wheel – 车轮 . chēlún
//        1205. observer – 观察者 . guānchá zhě
//        1206. tune – 调 . diào
//        1207. description – 说明 . shuōmíng
//        1208. establishment – 成立 . chénglì
//        1209. chamber – 室 . shì
//        1210. horror – 恐怖 . kǒngbù
//        1211. banking – 银行 . yínháng
//        1212. apple – 苹果 . píngguǒ
//        1213. implication – 寓意 . yùyì
//        1214. steel – 钢 . gāng
//        1215. throat – 喉咙 . hóulóng
//        1216. essence – 精华 . jīnghuá
//        1217. raid – 突袭 . túxí
//        1218. distinction – 区别 . qūbié
//        1219. german – 德国 . déguó
//        1220. lip – 唇 . chún
//        1221. timing – 时间 . shíjiān
//        1222. insight – 敏锐的洞察力 . mǐnruì de dòngchá lì
//        1223. coup – 政变 . zhèngbiàn
//        1224. shift – 移位 . yí wèi
//        1225. policeman – 警察 . jǐngchá
//        1226. habit – 习惯 . xíguàn
//        1227. sheet – 板材 . bǎncái
//        1228. offender – 罪犯 . zuìfàn
//        1229. midnight – 午夜 . wǔyè
//        1230. execution – 执行 . zhíxíng
//        1231. anxiety – 焦虑 . jiāolǜ
//        1232. enterprise – 企业 . qǐyè
//        1233. strip – 带钢 . dài gāng
//        1234. pop – 弹出 . dànchū
//        1235. wonder – 奇迹 . qíjī
//        1236. shadow – 影子 . yǐngzi
//        1237. liberty – 自由 . zìyóu
//        1238. fiction – 小说 . xiǎoshuō
//        1239. intervention – 干预 . gānyù
//        1240. creation – 创作 . chuàngzuò
//        1241. marketing – 市场营销 . shìchǎng yíngxiāo
//        1242. christian – 基督教 . jīdūjiào
//        1243. pot – 锅 . guō
//        1244. fishing – 钓鱼 . diàoyú
//        1245. breakfast – 早餐 . zǎocān
//        1246. tube – 管 . guǎn
//        1247. determination – 确定 . quèdìng
//        1248. sand – 沙 . shā
//        1249. permission – 许可 . xǔkě
//        1250. guideline – 指引 . zhǐyǐn
//        1251. shame – 羞 . xiū
//        1252. riot – 骚乱 . sāoluàn
//        1253. user – 用户 . yònghù
//        1254. organ – 器官 . qìguān
//        1255. resort – 度假村 . dùjiàcūn
//        1256. designer – 设计师 . shèjì shī
//        1257. comparison – 比较 . bǐjiào
//        1258. employment – 就业 . jiùyè
//        1259. bread – 面包 . miànbāo
//        1260. wish – 希望 . xīwàng
//        1261. lover – 情人 . qíngrén
//        1262. jacket – 夹克 . jiákè
//        1263. pass – 通 . tōng
//        1264. statute – 章程 . zhāngchéng
//        1265. pace – 步伐 . bùfá
//        1266. frame – 框架 . kuàngjià
//        1267. confusion – 混乱 . hǔnluàn
//        1268. guarantee – 保证 . bǎozhèng
//        1269. commissioner – 专员 . zhuānyuán
//        1270. flame – 火焰 . huǒyàn
//        1271. assumption – 假设 . jiǎshè
//        1272. graduate – 研究生 . yánjiūshēng
//        1273. entry – 入门 . rùmén
//        1274. ward – 病房 . bìngfáng
//        1275. win – 赢 . yíng
//        1276. opera – 歌剧 . gējù
//        1277. friendship – 友谊 . yǒuyì
//        1278. exposure – 曝光 . pùguāng
//        1279. pig – 猪 . zhū
//        1280. mate – 大副 . dà fù
//        1281. nerve – 神经 . shénjīng
//        1282. length – 长度 . chángdù
//        1283. cow – 牛 . niú
//        1284. pack – 包 . bāo
//        1285. instruction – 指令 . zhǐlìng
//        1286. collapse – 崩溃 . bēngkuì
//        1287. assistant – 助手 . zhùshǒu
//        1288. objection – 异议 . yìyì
//        1289. diary – 日记 . rìjì
//        1290. scholar – 学者 . xuézhě
//        1291. comfort – 舒适 . shūshì
//        1292. preparation – 准备 . zhǔnbèi
//        1293. province – 全省 . quán shěng
//        1294. passage – 通过 . tōngguò
//        1295. crystal – 晶 . jīng
//        1296. gear – 齿轮 . chǐlún
//        1297. stroke – 行程 . xíngchéng
//        1298. decline – 下降 . xiàjiàng
//        1299. achievement – 成就 . chéngjiù
//        1300. existence – 存在 . cúnzài
//        1301. alliance – 联盟 . liánméng
//        1302. profession – 行业 . hángyè
//        1303. delivery – 交付 . jiāofù
//        1304. publisher – 出版商 . chūbǎn shāng
//        1305. sympathy – 同情 . tóngqíng
//        1306. dish – 菜 . cài
//        1307. worry – 忧 . yōu
//        1308. component – 该组件 . gāi zǔjiàn
//        1309. adventure – 冒险 . màoxiǎn
//        1310. crop – 作物 . zuòwù
//        1311. blow – 打击 . dǎjí
//        1312. sake – 为了 . wèile
//        1313. sin – 罪 . zuì
//        1314. pension – 养老金 . yǎnglǎo jīn
//        1315. garage – 车库 . chēkù
//        1316. radiation – 辐射 . fúshè
//        1317. juice – 果汁 . guǒzhī
//        1318. setting – 设置 . shèzhì
//        1319. index – 索引 . suǒyǐn
//        1320. vegetable – 蔬菜 . shūcài
//        1321. partnership – 该合作伙伴关系 . gāi hézuò huǒbàn guānxì
//        1322. poetry – 诗歌 . shīgē
//        1323. recognition – 识别 . shìbié
//        1324. self – 自我 . zìwǒ
//        1325. standing – 站立 . zhànlì
//        1326. tissue – 组织 . zǔzhī
//        1327. shell – 外壳 . wàiké
//        1328. captain – 队长 . duìzhǎng
//        1329. grave – 坟墓 . fénmù
//        1330. rank – 军衔 . jūnxián
//        1331. gender – 性别 . xìngbié
//        1332. shortage – 短缺 . duǎnquē
//        1333. prize – 奖品 . jiǎngpǐn
//        1334. coat – 大衣 . dàyī
//        1335. seed – 种子 . zhǒngzǐ
//        1336. rat – 大鼠 . dà shǔ
//        1337. soil – 土壤 . tǔrǎng
//        1338. shore – 岸边 . àn biān
//        1339. defeat – 失败 . shībài
//        1340. notice – 通知 . tōngzhī
//        1341. boot – 开机 . kāijī
//        1342. chart – 图表 . túbiǎo
//        1343. ministry – 该部 . gāi bù
//        1344. diamond – 钻石 . zuànshí
//        1345. finish – 终点 . zhōngdiǎn
//        1346. admission – 录取 . lùqǔ
//        1347. dark – 黑暗 . hēi'àn
//        1348. laboratory – 实验室 . shíyàn shì
//        1349. professional – 专业 . zhuānyè
//        1350. reward – 奖励 . jiǎnglì
//        1351. infant – 婴儿 . yīng'ér
//        1352. text – 文本 . wénběn
//        1353. shower – 淋浴 . línyù
//        1354. dose – 剂量 . jìliàng
//        1355. panic – 恐慌 . kǒnghuāng
//        1356. net – 净 . jìng
//        1357. clerk – 店员 . diànyuán
//        1358. miner – 矿工 . kuànggōng
//        1359. delegation – 代表团 . dàibiǎo tuán
//        1360. imagination – 想象 . xiǎngxiàng
//        1361. unity – 团结 . tuánjié
//        1362. uncle – 叔叔 . shūshu
//        1363. ingredient – 成分 . chéngfèn
//        1364. assignment – 分配 . fēnpèi
//        1365. currency – 货币 . huòbì
//        1366. battery – 电池 . diànchí
//        1367. tunnel – 隧道 . suìdào
//        1368. resignation – 辞职 . cízhí
//        1369. buyer – 买家 . mǎi jiā
//        1370. bike – 自行车 . zìxíngchē
//        1371. excitement – 兴奋 . xīngfèn
//        1372. mouse – 鼠标 . shǔbiāo
//        1373. alarm – 报警 . bàojǐng
//        1374. poet – 诗人 . shīrén
//        1375. landscape – 景观 . jǐngguān
//        1376. selection – 选择 . xuǎnzé
//        1377. cry – 哭 . kū
//        1378. separation – 分离 . fēnlí
//        1379. display – 显示器 . xiǎnshìqì
//        1380. disability – 残疾 . cánjí
//        1381. counter – 计数器 . jìshùqì
//        1382. championship – 冠军 . guànjūn
//        1383. butter – 黄油 . huángyóu
//        1384. portrait – 画像 . huàxiàng
//        1385. stick – 棒 . bàng
//        1386. engineering – 工程 . gōngchéng
//        1387. myth – 神话 . shénhuà
//        1388. essay – 作文 . zuòwén
//        1389. cold – 冷 . lěng
//        1390. coal – 煤炭 . méitàn
//        1391. contrast – 对比 . duìbǐ
//        1392. observation – 观察 . guānchá
//        1393. tail – 尾 . wěi
//        1394. mechanism – 机制 . jīzhì
//        1395. scheme – 该计划 . gāi jìhuà
//        1396. presentation – 演示 . yǎnshì
//        1397. angel – 天使 . tiānshǐ
//        1398. bonus – 奖金 . jiǎngjīn
//        1399. possession – 藏 . cáng
//        1400. learning – 学习 . xuéxí
//        1401. teaching – 教学 . jiàoxué
//        1402. deck – 甲板 . jiǎbǎn
//        1403. creature – 生物 . shēngwù
//        1404. running – 运行 . yùnxíng
//        1405. literature – 文献 . wénxiàn
//        1406. weakness – 弱点 . ruòdiǎn
//        1407. blue – 蓝色 . lán sè
//        1408. engagement – 订婚 . dìnghūn
//        1409. uncertainty – 不确定性 . bù quèdìng xìng
//        1410. inquiry – 查询 . cháxún
//        1411. motivation – 动力 . dònglì
//        1412. dust – 灰尘 . huīchén
//        1413. peak – 高峰 . gāofēng
//        1414. instinct – 本能 . běnnéng
//        1415. needle – 针 . zhēn
//        1416. leaf – 叶子 . yèzi
//        1417. launch – 推出 . tuīchū
//        1418. laugh – 笑 . xiào
//        1419. leave – 请假 . qǐngjià
//        1420. grass – 草 . cǎo
//        1421. iron – 铁 . tiě
//        1422. competitor – 竞争对手 . jìngzhēng duìshǒu
//        1423. complex – 复杂 . fùzá
//        1424. bean – 豆 . dòu
//        1425. pole – 极 . jí
//        1426. vessel – 该船 . gāi chuán
//        1427. subsidy – 补贴 . bǔtiē
//        1428. bid – 中标 . zhòngbiāo
//        1429. landing – 登陆 . dēnglù
//        1430. pipe – 管道 . guǎndào
//        1431. interpretation – 解读 . jiědú
//        1432. tournament – 赛事 . sàishì
//        1433. hip – 臀部 . túnbù
//        1434. diagnosis – 诊断 . zhěnduàn
//        1435. pitch – 球场 . qiúchǎng
//        1436. journal – 该杂志 . gāi zázhì
//        1437. ceiling – 天花板 . tiānhuābǎn
//        1438. export – 出口 . chūkǒu
//        1439. awareness – 意识 . yìshí
//        1440. cross – 十字 . shízì
//        1441. kiss – 吻 . wěn
//        1442. examination – 考试 . kǎoshì
//        1443. empire – 帝国 . dìguó
//        1444. replacement – 更换 . gēnghuàn
//        1445. duck – 鸭 . yā
//        1446. formula – 公式 . gōngshì
//        1447. devil – 魔鬼 . móguǐ
//        1448. limitation – 限制 . xiànzhì
//        1449. compensation – 赔偿 . péicháng
//        1450. invitation – 邀请 . yāoqǐng
//        1451. declaration – 声明 . shēngmíng
//        1452. significance – 意义 . yìyì
//        1453. final – 最后 . zuìhòu
//        1454. premium – 保费 . bǎofèi
//        1455. ambulance – 救护车 . jiùhù chē
//        1456. carpet – 地毯 . dìtǎn
//        1457. temple – 寺庙 . sìmiào
//        1458. constituent – 组成 . zǔchéng
//        1459. concession – 特许经营 . tèxǔ jīngyíng
//        1460. consent – 同意 . tóngyì
//        1461. premise – 前提 . qiántí
//        1462. heritage – 遗产 . yíchǎn
//        1463. shelf – 货架 . huòjià
//        1464. hint – 提示 . tíshì
//        1465. rhythm – 节奏 . jiézòu
//        1466. liability – 责任 . zérèn
//        1467. heel – 脚跟 . jiǎogēn
//        1468. layer – 该层 . gāi céng
//        1469. strain – 应变 . yìngbiàn
//        1470. carbon – 碳 . tàn
//        1471. forum – 座谈会 . zuòtán huì
//        1472. merit – 优点 . yōudiǎn
//        1473. youngster – 童 . tóng
//        1474. transfer – 转移 . zhuǎnyí
//        1475. distribution – 分布 . fēnbù
//        1476. expansion – 扩张 . kuòzhāng
//        1477. depth – 深度 . shēndù
//        1478. blanket – 毯子 . tǎnzi
//        1479. kingdom – 王国 . wángguó
//        1480. mud – 泥 . ní
//        1481. arrival – 到货 . dào huò
//        1482. reflection – 反射 . fǎnshè
//        1483. envelope – 信封 . xìnfēng
//        1484. reserve – 储备 . chúbèi
//        1485. hunting – 狩猎 . shòuliè
//        1486. furniture – 家具 . jiājù
//        1487. gesture – 手势 . shǒushì
//        1488. residence – 住所 . zhùsuǒ
//        1489. ambition – 野心 . yěxīn
//        1490. nail – 钉 . dīng
//        1491. spectrum – 频谱 . pín pǔ
//        1492. drawing – 绘图 . huìtú
//        1493. enthusiasm – 热情 . rèqíng
//        1494. pen – 笔 . bǐ
//        1495. preference – 偏好 . piānhào
//        1496. absence – 没有 . méiyǒu
//        1497. oxygen – 氧 . yǎng
//        1498. squad – 球队 . qiú duì
//        1499. pollution – 污染 . wūrǎn
//        1500. lock – 锁 . suǒ
//        1501. extension – 扩展 . kuòzhǎn
//        1502. participant – 参与者 . cānyù zhě
//        1503. architect – 建筑师 . jiànzhú shī
//        1504. bench – 替补 . tìbǔ
//        1505. rope – 绳子 . shéngzi
//        1506. aim – 目的 . mùdì
//        1507. stream – 流 . liú
//        1508. evolution – 进化 . jìnhuà
//        1509. angle – 角 . jiǎo
//        1510. jurisdiction – 管辖 . guǎnxiá
//        1511. charter – 章程 . zhāngchéng
//        1512. ideology – 意识形态 . yìshí xíngtài
//        1513. tide – 潮 . cháo
//        1514. representation – 代表 . dàibiǎo
//        1515. identification – 鉴定 . jiàndìng
//        1516. concentration – 浓度 . nóngdù
//        1517. norm – 常态 . chángtài
//        1518. spread – 传播 . chuánbò
//        1519. rival – 对手 . duìshǒu
//        1520. disappointment – 失望 . shīwàng
//        1521. tribunal – 法庭 . fǎtíng
//        1522. grain – 粮食 . liángshí
//        1523. proportion – 比例 . bǐlì
//        1524. fool – 傻瓜 . shǎguā
//        1525. merger – 合并 . hébìng
//        1526. loyalty – 忠诚 . zhōngchéng
//        1527. publication – 出版 . chūbǎn
//        1528. participation – 参与 . cānyù
//        1529. escape – 逃生 . táoshēng
//        1530. shareholder – 股东 . gǔdōng
//        1531. constituency – 选区 . xuǎnqū
//        1532. paint – 油漆 . yóuqī
//        1533. toilet – 马桶 . mǎtǒng
//        1534. ghost – 鬼 . guǐ
//        1535. attraction – 吸引力 . xīyǐn lì
//        1536. lion – 狮子 . shīzi
//        1537. craft – 工艺 . gōngyì
//        1538. custom – 自定义 . zì dìng yì
//        1539. cliff – 悬崖 . xuányá
//        1540. palace – 宫殿 . gōngdiàn
//        1541. grip – 握 . wò
//        1542. protein – 蛋白质 . dànbáizhí
//        1543. accuracy – 准确度 . zhǔnquè dù
//        1544. theft – 盗窃 . dàoqiè
//        1545. widow – 寡妇 . guǎfù
//        1546. acceptance – 验收 . yànshōu
//        1547. accent – 雅绅特 . yǎshēntè
//        1548. smell – 闻 . wén
//        1549. departure – 出发 . chūfā
//        1550. membership – 会员 . huìyuán
//        1551. expertise – 专业知识 . zhuānyè zhīshì
//        1552. repair – 修复 . xiūfù
//        1553. assembly – 大会 . dàhuì
//        1554. summary – 摘要 . zhāiyào
//        1555. logic – 逻辑 . luójí
//        1556. boundary – 边界 . biānjiè
//        1557. equivalent – 相当于 . xiāngdāng yú
//        1558. holder – 持有人 . chí yǒu rén
//        1559. dimension – 尺寸 . chǐcùn
//        1560. rail – 铁路 . tiělù
//        1561. transaction – 交易 . jiāoyì
//        1562. left – 左 . zuǒ
//        1563. sequence – 序列 . xùliè
//        1564. festival – 节日 . jiérì
//        1565. pit – 坑 . kēng
//        1566. horizon – 地平线 . dìpíngxiàn
//        1567. bowl – 碗 . wǎn
//        1568. purchase – 购买 . gòumǎi
//        1569. plaintiff – 原告 . yuángào
//        1570. aunt – 阿姨 . āyí
//        1571. equity – 股权 . gǔquán
//        1572. load – 负载 . fùzǎi
//        1573. scope – 范围 . fànwéi
//        1574. darkness – 黑暗 . hēi'àn
//        1575. sandwich – 三明治 . sānmíngzhì
//        1576. parish – 教区 . jiàoqū
//        1577. closure – 关闭 . guānbì
//        1578. pile – 桩 . zhuāng
//        1579. ownership – 所有权 . suǒyǒuquán
//        1580. origin – 起源 . qǐyuán
//        1581. defender – 后卫 . hòuwèi
//        1582. circuit – 电路 . diànlù
//        1583. introduction – 介绍 . jièshào
//        1584. mode – 模式 . móshì
//        1585. discount – 打折 . dǎzhé
//        1586. equation – 方程 . fāngchéng
//        1587. entity – 实体 . shítǐ
//        1588. kit – 该套件 . gāi tàojiàn
//        1589. psychology – 心理学 . xīnlǐ xué
//        1590. virtue – 凭借 . píngjiè
//        1591. flexibility – 灵活性 . línghuó xìng
//        1592. trainer – 教练 . jiàoliàn
//        1593. continent – 大陆 . dàlù
//        1594. trader – 交易者 . jiāo yì zhě
//        1595. bath – 洗澡 . xǐzǎo
//        1596. brick – 砖 . zhuān
//        1597. indicator – 该指标 . gāi zhǐbiāo
//        1598. seller – 卖家 . màijiā
//        1599. promotion – 促销 . cùxiāo
//        1600. clash – 冲突 . chōngtú
//        1601. characteristic – 特点 . tèdiǎn
//        1602. coin – 硬币 . yìngbì
//        1603. assurance – 保证 . bǎozhèng
//        1604. proposition – 命题 . mìngtí
//        1605. tongue – 舌头 . shétou
//        1606. minimum – 最小 . zuìxiǎo
//        1607. collector – 集电极 . jí diànjí
//        1608. capitalism – 资本主义 . zīběn zhǔyì
//        1609. basket – 篮下 . lán xià
//        1610. framework – 该框架 . gāi kuàngjià
//        1611. consciousness – 意识 . yìshí
//        1612. switch – 开关 . kāiguān
//        1613. rent – 租金 . zūjīn
//        1614. curve – 曲线 . qūxiàn
//        1615. cotton – 棉花 . miánhuā
//        1616. patch – 补丁 . bǔdīng
//        1617. import – 进口 . jìnkǒu
//        1618. isolation – 隔离 . gélí
//        1619. entrance – 高考 . gāokǎo
//        1620. necessity – 必要性 . bìyào xìng
//        1621. square – 广场 . guǎngchǎng
//        1622. venture – 创业 . chuàngyè
//        1623. steam – 蒸汽 . zhēngqì
//        1624. agriculture – 农业 . nóngyè
//        1625. transformation – 改造 . gǎizào
//        1626. sheep – 羊 . yáng
//        1627. brush – 刷 . shuā
//        1628. database – 数据库 . shùjùkù
//        1629. deposit – 存款 . cúnkuǎn
//        1630. menu – 菜单 . càidān
//        1631. adjustment – 调整 . tiáozhěng
//        1632. triumph – 胜利 . shènglì
//        1633. consumption – 消费 . xiāofèi
//        1634. guardian – 守护者 . shǒuhù zhě
//        1635. curtain – 窗帘 . chuānglián
//        1636. satisfaction – 满意 . mǎnyì
//        1637. calculation – 计算 . jìsuàn
//        1638. fleet – 车队 . chēduì
//        1639. remedy – 补救措施 . bǔjiù cuòshī
//        1640. lane – 车道 . chēdào
//        1641. certificate – 证书 . zhèngshū
//        1642. swimming – 游泳 . yóuyǒng
//        1643. emission – 发射 . fāshè
//        1644. fabric – 面料 . miànliào
//        1645. medium – 媒体 . méitǐ
//        1646. efficiency – 效率 . xiàolǜ
//        1647. interaction – 互动 . hùdòng
//        1648. cab – 驾驶室 . jiàshǐ shì
//        1649. easter – 复活节 . fùhuó jié
//        1650. glory – 荣耀 . róngyào
//        1651. reception – 接待处 . jiēdài chù
//        1652. productivity – 生产力 . shēngchǎnlì
//        1653. acid – 酸 . suān
//        1654. servant – 仆人 . púrén
//        1655. evaluation – 评估 . pínggū
//        1656. mill – 磨 . mó
//        1657. exhibition – 展览 . zhǎnlǎn
//        1658. sum – 总和 . zǒnghé
//        1659. counterpart – 对口 . duìkǒu
//        1660. sergeant – 警长 . jǐng zhǎng
//        1661. storage – 存储 . cúnchú
//        1662. fraction – 分数 . fēnshù
//        1663. innovation – 创新 . chuàng xīn
//        1664. stance – 立场 . lìchǎng
//        1665. motor – 马达 . mǎdá
//        1666. format – 格式 . géshì
//        1667. particle – 颗粒 . kēlì
//        1668. trace – 跟踪 . gēnzōng
//        1669. fluid – 流体 . liútǐ
//        1670. curriculum – 课程设置 . kèchéng shèzhì
//        1671. doctrine – 学说 . xuéshuō
//        1672. developer – 开发商 . kāifā shāng
//        1673. pause – 暂停 . zàntíng
//        1674. removal – 拆除 . chāichú
//        1675. sensation – 感觉 . gǎnjué
//        1676. dividend – 股息 . gǔxí
//        1677. monopoly – 垄断 . lǒngduàn
//        1678. gallery – 图库 . túkù
//        1679. lecture – 讲座 . jiǎngzuò
//        1680. taxi – 出租车 . chūzū chē
//        1681. discretion – 自由裁量权 . zìyóu cáiliáng quán
//        1682. flesh – 肉 . ròu
//        1683. merchant – 商家 . shāngjiā
//        1684. quantity – 数量 . shùliàng
//        1685. darling – 宠儿 . chǒng'ér
//        1686. justification – 理由 . lǐyóu
//        1687. painter – 画家 . huàjiā
//        1688. mixture – 混合物 . hùnhéwù
//        1689. breach – 违约 . wéiyuē
//        1690. lift – 电梯 . diàntī
//        1691. pond – 池塘 . chítáng
//        1692. cheek – 脸颊 . liǎnjiá
//        1693. successor – 继任者 . jìrèn zhě
//        1694. registration – 登记 . dēngjì
//        1695. consultation – 咨询 . zīxún
//        1696. qualification – 资格 . zīgé
//        1697. supplier – 供应商 . gōngyìng shāng
//        1698. autumn – 秋季 . qiūjì
//        1699. hardware – 硬件 . yìngjiàn
//        1700. leather – 皮革 . pígé
//        1701. rabbit – 兔子 . tùzǐ
//        1702. paragraph – 段落 . duànluò
//        1703. companion – 同伴 . tóngbàn
//        1704. bulk – 大头 . dàtóu
//        1705. corridor – 走廊 . zǒuláng
//        1706. workshop – 车间 . chējiān
//        1707. shade – 树荫 . shù yīn
//        1708. accountant – 会计 . kuàijì
//        1709. sword – 剑 . jiàn
//        1710. formation – 形成 . xíngchéng
//        1711. integration – 整合 . zhěnghé
//        1712. expenditure – 支出 . zhīchū
//        1713. input – 输入 . shūrù
//        1714. rod – 杆 . gān
//        1715. complexity – 复杂性 . fùzá xìng
//        1716. skirt – 裙子 . qúnzi
//        1717. insect – 昆虫 . kūnchóng
//        1718. transport – 运输 . yùnshū
//        1719. mortality – 死亡率 . sǐwáng lǜ
//        1720. slope – 坡 . pō
//        1721. restoration – 恢复 . huīfù
//        1722. nest – 巢 . cháo
//        1723. variation – 变化 . biànhuà
//        1724. composition – 组成 . zǔchéng
//        1725. crown – 冠 . guān
//        1726. probability – 概率 . gàilǜ
//        1727. liberation – 解放 . jiěfàng
//        1728. attendance – 出席 . chūxí
//        1729. organism – 有机体 . yǒujītǐ
//        1730. conservation – 保护 . bǎohù
//        1731. sculpture – 雕塑 . diāosù
//        1732. champagne – 香槟 . xiāngbīn
//        1733. architecture – 架构 . jiàgòu
//        1734. university – 大学 . dàxué
//        1735. thousand – 千元 . qiān yuán
//        1736. percent – 百分比 . bǎifēnbǐ
//        1737. bureau – 局 . jú
//        1738. king – 王 . wáng
//        1739. republic – 共和国 . gònghéguó
//        1740. god – 神 . shén
//        1741. dozen – 十几 . shí jǐ
//        1742. traveller – 旅行者 . lǚxíng zhě
//        1743. junction – 结 . jié
//        1744. fibre – 纤维 . xiānwéi
//        1745. interface – 接口 . jiēkǒu
//        1746. abbey – 修道院 . xiūdàoyuàn
//        1747. avenue – 大道 . dàdào
//        1748. directory – 目录 . mùlù
//        1749. whisky – 威士忌 . wēishìjì
//        1750. park – 公园 . gōngyuán
//        1751. dollar – 美元 . měiyuán
//        1752. lifespan – 寿命 . shòumìng
//        1753. princess – 公主 . gōngzhǔ
//        1754. colony – 殖民地 . zhímíndì
//        1755. million – 百万 . bǎi wàn
//        1756. hill – 小山 . xiǎoshān
//        1757. mp – 该熔点 . gāi róngdiǎn
//        1758. stairs – 楼梯 . lóutī
//        1759. petrol – 汽油 . qìyóu
//        1760. pope – 教皇 . jiàohuáng
//        1761. council – 理事会 . lǐshì huì
//        1762. valley – 山谷 . shāngǔ
//        1763. billion – 数十亿 . shù shí yì
//        1764. van – 面包车 . miànbāochē
//        1765. humour – 幽默 . yōumò
//        1766. gaze – 凝视 . níngshì
//        1767. tory – 保守党 . bǎoshǒu dǎng
//        1768. eagle – 老鹰 . lǎoyīng
//        1769. tutor – 导师 . dǎoshī
//        1770. bill – 该法案 . gāi fǎ'àn
//        1771. photograph – 照片 . zhàopiàn
//        1772. inch – 寸 . cùn
//        1773. enquiry – 查询 . cháxún
//        1774. white – 白 . bái
//        1775. polytechnic – 理工学院 . lǐgōng xuéyuàn
//        1776. inn – 客栈 . kèzhàn
//        1777. mrs – 该女士 . gāi nǚshì
//        1778. earth – 大地 . dàdì
//        1779. theatre – 剧院 . jùyuàn
//        1780. ref – 裁判 . cáipàn
//        1781. earl – 伯爵 . bójué
//        1782. isle – 小岛 . xiǎo dǎo
//        1783. chap – 在第一章 . zài dì yī zhāng
//        1784. appendix – 附录 . fùlù
//        1785. institute – 该研究所 . gāi yánjiū suǒ
//        1786. corp – 该CORP . gāi CORP
//        1787. constitution – 宪法 . xiànfǎ
//        1788. colonel – 上校 . shàngxiào
//        1789. favour – 青睐 . qīnglài
//        1790. magistrate – 知县 . zhīxiàn
//        1791. favourite – 最爱 . zuì ài
//        1792. illustration – 插图 . chātú
//        1793. mum – 在妈妈 . zài māmā
//        1794. criterion – 标准 . biāozhǔn
//        1795. lake – 湖 . hú
//        1796. co – 合作 . hézuò
//        1797. lad – 小伙子 . xiǎohuǒzi
//        1798. specification – 该规范 . gāi guīfàn
//        1799. ms – 在毫秒 . zài háomiǎo
//        1800. redundancy – 冗余 . rǒng yú
//        1801. centre – 中心 . zhōngxīn
//        1802. fig – 无花果 . wúhuāguǒ
//        1803. mummy – 木乃伊 . mùnǎiyī
//        1804. federation – 联邦 . liánbāng
//        1805. other – 其他 . qítā
//        1806. solicitor – 律师 . lǜshī
//        1807. honour – 荣誉 . róngyù
//        1808. red – 红 . hóng
//        1809. constable – 警员 . jǐng yuán
//        1810. navy – 海军 . hǎijūn
//        1811. harbour – 海港 . hǎigǎng
//        1812. sociology – 社会学 . shèhuì xué
//        1813. builder – 建设者 . jiànshè zhě
//        1814. offence – 进攻 . jìngōng
//        1815. duke – 公爵 . gōngjué
//        1816. photo – 照片 . zhàopiàn
//        1817. rumour – 传闻 . chuánwén
//        1818. classification – 分类 . fēnlèi
//        1819. programme – 该程序 . gāi chéngxù
//        1820. hundred – 百 . bǎi
//        1821. county – 县 . xiàn
//        1822. flat – 平 . píng
//        1823. tv – 电视 . diànshì
//        1824. councillor – 该委员 . gāi wěiyuán
//        1825. metre – 仪表 . yíbiǎo
//        1826. bay – 海湾 . hǎiwān
//        1827. rubbish – 垃圾 . lèsè
//        1828. lorry – 货车 . huòchē
//        1829. cupboard – 橱柜 . chúguì
//        1830. mm – 该毫米 . gāi háomǐ
//        1831. association – 该协会 . gāi xiéhuì
//        1832. organisation – 该组织 . gāi zǔzhī
//        1833. mile – 一英里 . yī yīnglǐ
//        1834. miss – 思念 . sīniàn
//        1835. queen – 女王 . nǚwáng
//        1836. judgement – 判决 . pànjué
//        1837. catalogue – 目录 . mùlù
//        1838. villa – 别墅 . biéshù
//        1839. bush – 布什 . bùshí
//        1840. labour – 劳动 . láodòng
//        1841. defence – 防御 . fángyù
//        1842. mark – 标记 . biāojì
//        1843. neighbour – 邻居 . línjū
//        1844. rose – 玫瑰 . méiguī
//        1845. cheque – 检查 . jiǎnchá
//        1846. borough – 自治市镇 . zìzhì shì zhèn
//        1847. purchaser – 购买 . gòumǎi
//        1848. daddy – 爸爸 . bàba
//        1849. go – 旅途 . lǚtú
//        1850. shit – 狗屎 . gǒu shǐ
//        1851. commonwealth – 英联邦 . yīng liánbāng
//        1852. prince – 王子 . wángzǐ
//        1853. vat – 增值税 . zēngzhí shuì
//        1854. allocation – 配置 . pèizhì
//        1855. cm – 该厘米 . gāi límǐ
//        1856. pensioner – 领取养老金 . lǐngqǔ yǎnglǎo jīn
//        1857. aids – 艾滋病 . àizībìng
//        1858. colour – 颜色 . yánsè
//        1859. no – 无 . wú
//        1860. unix – 在UNIX . zài UNIX
//        1861. treasury – 国库 . guókù
//        1862. acre – 英亩 . yīngmǔ
//        1863. jew – 犹太人 . yóutàirén
//        1864. tonne – 在吨 . zài dūn
//        1865. archbishop – 大主教 . dàzhǔjiào
//        1866. lord – 主 . zhǔ
//        1867. behaviour – 行为 . xíngwéi
//        1868. licence – 许可 . xǔkě
//        1869. proceeding – 诉讼 . sùsòng
//        1870. terrace – 露台 . lùtái
//        1871. frequency – 频率 . pínlǜ
//        1872. seminar – 研讨会 . yántǎo huì
//        1873. interior – 内饰 . nèi shì
//        1874. verse – 诗句 . shījù
//        1875. receipt – 收据 . shōujù
//        1876. breed – 品种 . pǐnzhǒng
//        1877. measurement – 测量 . cè liáng
//        1878. palm – 手掌 . shǒuzhǎng
//        1879. dock – 码头 . mǎtóu
//        1880. pc – 个人电脑 . gèrén diànnǎo
//        1881. disadvantage – 缺点 . quēdiǎn
//        1882. installation – 安装 . ānzhuāng
//        1883. advertisement – 广告 . guǎnggào
//        1884. implementation – 实施 . shíshī
//        1885. portfolio – 投资组合 . tóuzī zǔhé
//        1886. timber – 木材 . mùcái
//        1887. clause – 该条款 . gāi tiáokuǎn
//        1888. discourse – 话语 . huàyǔ
//        1889. disc – 光盘 . guāngpán
//        1890. blade – 刀片 . dāopiàn
//        1891. ratio – 比 . bǐ
//        1892. mineral – 矿产 . kuàngchǎn
//        1893. outline – 大纲 . dàgāng
//        1894. addition – 加 . jiā
//        1895. machinery – 机械 . jīxiè
//        1896. canal – 运河 . yùnhé
//        1897. local – 当地 . dāngdì
//        1898. disposal – 处置 . chǔzhì
//        1899. refusal – 拒绝 . jùjué
//        1900. domain – 域 . yù
//        1901. register – 寄存器 . jìcúnqì
//        1902. audit – 审计 . shěnjì
//        1903. availability – 可用性 . kěyòngxìng
//        1904. acquisition – 收购 . shōugòu
//        1905. autonomy – 自主 . zìzhǔ
//        1906. silk – 丝绸 . sīchóu
//        1907. beam – 梁 . liáng
//        1908. accommodation – 住宿 . zhùsù
//        1909. applicant – 申请人 . shēnqǐng rén
//        1910. modification – 修改 . xiūgǎi
//        1911. tin – 锡 . xī
//        1912. fragment – 片段 . piànduàn
//        1913. tenant – 房客 . fángkè
//        1914. conversion – 转换 . zhuǎnhuàn
//        1915. vendor – 供应商 . gōngyìng shāng
//        1916. practitioner – 从业 . cóngyè
//        1917. delight – 喜悦 . xǐyuè
//        1918. filter – 过滤器 . guòlǜ qì
//        1919. bronze – 铜牌 . tóngpái
//        1920. doorway – 门口 . ménkǒu
//        1921. incidence – 发病率 . fābìng lǜ
//        1922. lease – 租赁 . zūlìn
//        1923. constraint – 约束 . yuēshù
//        1924. cloth – 布 . bù
//        1925. nursery – 幼儿园 . yòu ér yuán
//        1926. creditor – 债权人 . zhàiquánrén
//        1927. taxation – 税收 . shuìshōu
//        1928. disk – 磁盘 . cípán
//        1929. dictionary – 词典 . cídiǎn
//        1930. conception – 概念 . gàiniàn
//        1931. trustee – 受托人 . shòutuō rén
//        1932. molecule – 分子 . fēnzǐ
//        1933. holding – 控股 . kònggǔ
//        1934. cottage – 山寨 . shānzhài
//        1935. landlord – 房东 . fángdōng
//        1936. succession – 继承 . jìchéng
//        1937. warmth – 温暖 . wēnnuǎn
//        1938. spell – 法术 . fǎshù
//        1939. chapel – 礼拜堂 . lǐbàitáng
//        1940. leisure – 休闲 . xiūxián
//        1941. reign – 统治 . tǒngzhì
//        1942. reply – 回复 . huífù
//        1943. duration – 时间 . shíjiān
//        1944. castle – 城堡 . chéngbǎo
//        1945. hierarchy – 层次结构 . céngcì jiégòu
//        1946. correlation – 相关 . xiāngguān
//        1947. exclusion – 排除 . páichú
//        1948. pub – 酒馆 . jiǔguǎn
//        1949. hypothesis – 假设 . jiǎshè
//        1950. lamp – 灯 . dēng
//        1951. sphere – 球体 . qiútǐ
//        1952. chancellor – 校长 . xiàozhǎng
//        1953. peasant – 农民 . nóngmín
//        1954. output – 输出 . shūchū
//        1955. emperor – 皇帝 . huángdì
//        1956. glance – 扫视 . sǎoshì
//        1957. specimen – 试样 . shì yàng
//        1958. directive – 该指令 . gāi zhǐlìng
//        1959. thesis – 论文 . lùnwén
//        1960. working – 工作 . gōng zuò
//        1961. oak – 橡木 . xiàngmù
//        1962. turnover – 成交 . chéngjiāo
//        1963. allowance – 津贴 . jīntiē
//        1964. tray – 托盘 . tuōpán
//        1965. auditor – 核数师 . hé shù shī
//        1966. server – 服务器 . fúwùqì
//        1967. cathedral – 大教堂 . dà jiàotáng
//        1968. pity – 可惜 . kěxí
//        1969. variable – 变量 . biànliàng
//        1970. clay – 粘土 . niántǔ
//        1971. carriage – 马车 . mǎchē
//        1972. cinema – 电影院 . diànyǐngyuàn
//        1973. leaflet – 传单 . chuándān
//        1974. copper – 铜 . tóng
//        1975. density – 密度 . mìdù
//        1976. subsidiary – 子公司 . zǐ gōngsī
//        1977. dismissal – 解雇 . jiěgù
//        1978. completion – 完成 . wánchéng
//        1979. processor – 处理器 . chǔlǐ qì
//        1980. packet – 包 . bāo
//        1981. wool – 羊毛 . yángmáo
//        1982. heading – 标题 . biāotí
//        1983. grammar – 语法 . yǔfǎ
//        1984. pavement – 路面 . lùmiàn
//        1985. receiver – 接收器 . jiēshōu qì
//        1986. module – 模块 . mókuài
//        1987. railway – 铁路 . tiělù
//        1988. pupil – 瞳孔 . tóngkǒng
//        1989. cricket – 板球 . bǎn qiú
//        1990. printer – 打印机 . dǎyìnjī
//        1991. interval – 间隔 . jiàngé
//        1992. diagram – 图 . tú
//        1993. bastard – 混蛋 . húndàn