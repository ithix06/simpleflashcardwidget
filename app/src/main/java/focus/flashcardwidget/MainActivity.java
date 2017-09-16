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

        List<String> listOptions = new ArrayList<String>(Arrays.asList("Top 100 Chinese Words", "Top 100 Japanese Words", "Add Custom..."));
        ListView listView = (ListView)findViewById(R.id.mainListView);
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


                String chosenWords = "";

                if (position == 0 ) {
                    chosenWords = getIntegratedChinese();
                    Toast.makeText(getBaseContext(), str + " selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();
                } else if (position == 1) {
                    chosenWords = getJapanTop100();
                    Toast.makeText(getBaseContext(), str + " selected. Please Open Widget To Study Flashcards!", Toast.LENGTH_SHORT).show();
                }
                else if (str.equals("Add Custom...")) {

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
        if(requestCode==123 && resultCode==RESULT_OK) {
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

//    private void loadFileList() {
//        try {
//            mPath.mkdirs();
//        }
//        catch(SecurityException e) {
//            Log.e("TAG", "unable to write on the sd card " + e.toString());
//        }
//        if(mPath.exists()) {
//            FilenameFilter filter = new FilenameFilter() {
//
//                @Override
//                public boolean accept(File dir, String filename) {
//                    File sel = new File(dir, filename);
//                    return filename.contains(FTYPE) || sel.isDirectory();
//                }
//
//            };
//            mFileList = mPath.list(filter);
//        }
//        else {
//            mFileList= new String[0];
//        }
//    }
//
//    protected Dialog onCreateDialog(int id) {
//        Dialog dialog = null;
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        switch(id) {
//            case DIALOG_LOAD_FILE:
//                builder.setTitle("Choose your file");
//                if(mFileList == null) {
//                    Log.e("TAG", "Showing file picker before loading the file list");
//                    dialog = builder.create();
//                    return dialog;
//                }
//                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        mChosenFile = mFileList[which];
//                        //you can do stuff with the file here too
//                    }
//                });
//                break;
//        }
//        dialog = builder.show();
//        return dialog;
//    }


    public static String getJapanTop100() {
        String string = "";
        string += "トイレ, toire - Toilet\n";
        string += "どうして？, (doushite?) - Why?\n";
        string += "英語, (eigo) - English\n";
        string += "死にそう, (shini sou) - Seems Like I'm Going To Die\n";

        return string;

    }

    public static String getIntegratedChinese() {
        String string = "";
        string += "先生,xiānsheng –  mr; husband; teacher\n";
        string += "你好,nǐ hǎo – How do you du? Hello!\n";
        string += "小姐,xiǎojie – Miss; young lady\n";
        string += "请问,qǐngwèn –May I ask...\n";
        string += "您,nín – You(singular polite)\n";
        string += "您贵姓,nínguìxìng – What is your honorable surname?\n";
        string += "呢,ne - 	(an interrogative particle)\n";
        string += "叫,jiào - to be called; to call\n";
        string += " 什么,shénme	What?\n";
        string += " 名字,míngzi	Name\n";
        string += "是,shì - To be\n";
        string += "老师,Lǎoshī	Teacher\n";
        string += "吗,ma - (an interrogative particle)\n";
        string += "不,bù - not; no\n";
        string += "学生,xuésheng	student\n";
        string += "也,yě - too; also\n";
        string += "中国人,Zhōngguórén	Chinese people/person\n";
        string += "美国人,Měiguórén - American people/person\n";
        string += "那,nà/nèi	that\n";
        string += "张,zhāng	(a measure word for flat objects)\n";
        string += "照片,zhàopiàn	picture; photo\n";
        string += "的,de - (a possessive$ modifying or descriptive particle)\n";
        string += "这,zhè/zhèi	this\n";
        string += "爸爸,bàba - father; dad\n";
        string += "妈妈,māma	mother; mom\n";
        string += "这个,zhège	this\n";
        string += "男孩子,nánháizi	boy\n";
        string += " 谁,shéi	who\n";
        string += "他,tā - he; him\n";
        string += " 弟弟,dìdi - younger brother\n";
        string += " 女孩子,nǚháizi	girl\n";
        string += " 妹妹,mèimei	younger sister\n";
        string += " 她,tā	she; her\n";
        string += " 女儿,nǚ'ér  	daughter\n";
        string += " 有,yǒu	to have; to exist\n";
        string += " 儿子,érzi - son\n";
        string += " 没,méi	not\n";
        string += "家,jiā - family; home\n";
        string += "几,jǐ– - how many\n";
        string += "哥哥,gēge - older brother\n";
        string += "两,liǎng - two; a couple of\n";
        string += "姐姐,jiějie - older sister\n";
        string += "和,hé - and\n";
        string += "做,zuò - to do\n";
        string += "英文,Yīngwén	the English language\n";
        string += "律师,lǜshī	lawyer\n";
        string += " 都,dōu	both; all\n";
        string += " 大学生,dàxuéshēng	University; college\n";
        string += " 医生,yīshēng	doctor; physician\n";
        string += "九月,jiǔyuè	September\n";
        string += "十二,shí'èr - twelve\n";
        string += "号,hào - number in a series; day of the month\n";
        string += "星期四,xīngqīsì	Thursday\n";
        string += "天,tiān - day\n";
        string += "生日,shēngrì	Birthday\n";
        string += "今年,jīnnián	this year\n";
        string += "多大,duō dà	how old\n";
        string += "十八,shíbā - eighteen\n";
        string += " 岁,suì	year (of age)\n";
        string += " 请,qǐng	to treat (sombody); to invite\n";
        string += " 吃,chī	to eat\n";
        string += " 晚饭,wǎnfàn	dinner; supper\n";
        string += " 吃饭,chī fàn	to eat (a meal)\n";
        string += " 怎么样,zěnmeyàng	    Is it OK? What is it like? How does that sound?\n";
        string += " 太...了,tài...le	too; extremely\n";
        string += " 谢谢,xièxie	thank you\n";
        string += " 喜欢,xǐhuan	to like$ like to; to prefer$ prefer to\n";
        string += " 还是,háishi	or\n";
        string += " 可是,kěshì - but\n";
        string += " 好,hǎo	good; OK\n";
        string += " 我们,wǒmen	we; us\n";
        string += " 点钟,–diǎnzhōng	o'clock\n";
        string += " 半,bàn	half; half an hour\n";
        string += " 晚上,wǎnshang	evening; night\n";
        string += " 见,jiàn	to see\n";
        string += " 再见,zàijiàn	good bye; see you again\n";
        string += "现在,xiànzài	now\n";
        string += "刻,–kè - quarter (hour); 15 minutes\n";
        string += "事,shì - matter; affair; business\n";
        string += "明天,míngtiān	tomorrow\n";
        string += "忙,máng - busy\n";
        string += "今天,jiāntiān	today\n";
        string += "很,hěn - very\n";
        string += "为什么,wèishénme	why?\n";
        string += "因为,yīnwèi	because\n";
        string += " 还有,háiyǒu	also there are\n";
        string += " 同学,tóngxué	classmate\n";
        string += " 认识,rènshi	to know (someone); to recognize\n";
        string += "周末,zhōumò	weekend\n";
        string += "打球,dǎ qiú	to play ball\n";
        string += "看,kàn - to watch; to look\n";
        string += "电视,diànshì	TV\n";
        string += "唱歌,chàng gēr	to sing (a song)\n";
        string += "跳舞,tiào wǔ	to dance\n";
        string += "听,tīng - to listen\n";
        string += "音乐,yīnyuè	muisc\n";
        string += "对,duì - right; correct\n";
        string += " 有时候,yǒu shíhou	sometimes\n";
        string += " 看书,kàn shū	to read books; to read\n";
        string += " 电影,diànyǐng	movie\n";
        string += " 常常,chángcháng	often\n";
        string += " 那,nà	in that case; then\n";
        string += " 去,qù	to go\n";
        string += " 外国,wàiguó	foreign country\n";
        string += " 请客,qǐng kè	to invite someone to dinner; to be the host\n";
        string += " 昨天,zuótiān	yesterday\n";
        string += " 所以,suǒyǐ - so\n";
        string += "好久,hǎojiǔ	a long time\n";
        string += "不错,búcuò	not bad; pretty good\n";
        string += "想,xiǎng - to want to; to think; to miss\n";
        string += "觉得,juéde - to feel/think that...\n";
        string += "有意思,yǒuyìsi	interesting\n";
        string += "只,zhǐ - only\n";
        string += "睡觉,shuìjiào	to sleep\n";
        string += "算了,suàn le	forget it; never mind\n";
        string += "找,zhǎo - to look for\n";
        string += " 别人,biérén	others; other people; another person\n";
        string += "呀,ya - (an injectory particle used to soften a question)\n";
        string += "进,jìn - to enter\n";
        string += "快,kuài - fast; quick; quickly\n";
        string += "进来,jìnlai - to come in\n";
        string += "来,lái - to come\n";
        string += "介绍,jièshào	to introduce\n";
        string += "一下,yíxià	(a measure word used after a verb indicating short duration)\n";
        string += "高兴,gāoxìng	happy; pleased\n";
        string += "漂亮,piàoliang	pretty\n";
        string += " 坐,zuò	to sit\n";
        string += " 在,zài	at; in; on\n";
        string += " 哪儿,nǎr - where\n";
        string += " 工作,gōngzuò	to work; work; job\n";
        string += " 学校,xuéxiào	school\n";
        string += " 喝,hē	to drink\n";
        string += " 点儿,diǎn(r)	a little; a bit; some\n";
        string += " 茶,chá	tea\n";
        string += " 咖啡,kāfēi - coffee\n";
        string += " 啤酒,píjiǔ - beer\n";
        string += " 吧,ba	(a suggestion particle; softens the tone of the sentence to which it is appended)\n";
        string += " 要,yào	to want; to have a desire for\n";
        string += " 杯,bēi	cup; glass$ measure word\n";
        string += " 可乐,kělè - cola\n";
        string += " 可以,kěyǐ - can; may\n";
        string += " 对不起,duìbuqǐ	I'm sorry.\n";
        string += " 给,gěi	to give\n";
        string += " 水,shuǐ	water\n";
        string += "完儿,wán(r)	to have fun; to play\n";
        string += "图书馆,túshūguǎn	Library\n";
        string += "瓶,píng - bottle (measure word)\n";
        string += "一起,yìqǐ - together\n";
        string += "聊天,liáotiān(r)	to chat\n";
        string += "才,cái - not until$ only then\n";
        string += "回家,huíjiā - to go home\n";
        string += "帮忙,bāng máng	to help; to do someone a favor\n";
        string += "别客气,bié kèqi	don't be so polite!\n";
        string += "下个星期,xiàge xīngqī	Next week\n";
        string += "中文,Zhōngwén	the Chinese language\n";
        string += "帮,bāng - to help\n";
        string += "练习,liànxí - to practice\n";
        string += "说,shuō - to say; to speak\n";
        string += "啊,a - (a sentence–final particle of exclamation$ interrogation)\n";
        string += "但是,dànshì	but\n";
        string += " 得,děi	must; to have to\n";
        string += " 知道,zhīdao	to know\n";
        string += " 回来,huílai - to come back\n";
        string += "跟,gēn - and\n";
        string += "说话,shuō huà	to talk (vo)\n";
        string += "上个星期,shàngge xīngqī last week\n";
        string += "得,de - a structural particle\n";
        string += "帮助,bāngzhù	to help\n";
        string += "# 复习,fùxí - to review\n";
        string += "字,zì - character\n";
        string += "写,xiě - to write\n";
        string += "慢,màn - slow\n";
        string += " 教,jiāo - to teach\n";
        string += " 怎么,zěnme	how\n";
        string += " 就,jiù	(ADV indicates that something takes place sooner than expected)\n";
        string += " 学,xué - to study\n";
        string += " 笔,bǐ - pen\n";
        string += " 难,nán - difficult\n";
        string += " 快,kuài - quick$ fast\n";
        string += " 哪里,nǎli	you flatter me. Not at all. (A polite reply to a compliment).\n";
        string += " 第,dì - (a prefix for ordinal numbers)\n";
        string += " # 预习,yùxí - to preview\n";
        string += " 语法,yǔfǎ - grammar\n";
        string += " 容易,róngyì	easy\n";
        string += " 多,duō - many; much\n";
        string += " 懂,dǒng - to understand\n";
        string += " 生词,shēngcí	new words\n";
        string += " 汉字,hànzì - Chinese Character\n";
        string += " 有一点儿,yǒu yìdiǎnr 	a little; somewhat\n";
        string += " 不谢,búxiè - Don't mention it. Not at all$ you're welcstring += \n";
        return string;
    }
}
