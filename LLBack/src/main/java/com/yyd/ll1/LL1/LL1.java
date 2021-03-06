package com.yyd.ll1.LL1;

import com.yyd.ll1.enity.Data;
import com.yyd.ll1.enity.ResultData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class LL1 {
    /**
     * 文法:
     * E->TG
     * G->+TG|-TG
     * G->@
     * T->FS
     * S->*FS|/FS
     * S->@
     * F->(E)
     * F->i
     */


    // = {"+", "-", "*", "/", "(", ")", "$", "#", "i"}
    //  {"E", "G", "T", "S", "F"}
    //终结符
    private String[] VT;
    //非终结符
    private String[] VN;

    //开始符
    private String startChar = "E";

    //输入句子
    private String string;
    //句子读取位置标识
    private int index;

    private Stack<String> stack = new Stack<>();

    //运行结束标志
    private boolean runFlag = true;

    //临时存储字符
    private String tempCh;

    //存储栈中弹出的非终结符
    private String X;

    //分析表
    private Map<String, String> M;
    private Map<String, List<String>> First;
    private Map<String,List<String>> Follow;

    private Map<String, List<String>> GS;

    //输出数据表
    private List<List<String>> outData = new ArrayList<>();

    /**
     * 封装分析表返回信息
     * @return
     */
    public  List<List<String>> getData(){

        List<List<String>> result = new ArrayList<>();

        List<String> tempNT = new ArrayList<>();
        Arrays.stream(VT).forEach((String s)->tempNT.add(s));
        tempNT.add("#");

        List<String> L = new ArrayList<>();
        L.add(" ");
        for(String s : tempNT){
            if(!s.equals("$")){
                System.out.println(s);
                L.add(s);
            }

        }
        result.add(L);

        for(String s : VN){
            List<String> list = new ArrayList<>();
            list.add(s);
            result.add(list);
        }

        int i = 1;

        for(String s1 : VN){
            for(String s2 : tempNT){
                if(!s2.equals("$")){
                    if(M.get(s1+":"+s2)!=null){
                        result.get(i).add(M.get(s1+":"+s2));
                    }else {
                        result.get(i).add(" ");
                    }
                }
            }
            i++;
        }



        return result;
    }

    /**
     * 流程控制
     * @param
     */
    public Object run() throws IOException {

        if(GS.keySet().size() == 0){
            System.out.println("文法创建失败, 无法运行");
            return null;
        }

        //生成分析表
        initM();

        //初始化第一行
        List<String> L = new ArrayList<>();
        L.add("1");
        L.add("#E");
        L.add(string);
        L.add("");
        L.add("初始化");
        outData.add(L);
        stack.push(startChar);
        //动作存储表
        StringBuffer action = new StringBuffer();

        int i = 1;
        index = 0;
        tempCh = getNextChar();
        while (runFlag){
            List<String> rowOut = new ArrayList<>();
            this.X = stack.pop();
            action.delete(0, action.length());
            String creatStr = "";
            if (Util.isInArray(VT, X) && !X.equals("$")){

                if (X.equals(tempCh)) {
                    action.append("GETNEXT");
                    index++;
                    tempCh = getNextChar();

                }
                else System.out.println("输入字符串中包含未识别字符");

            } else if(X.equals("#")){
                if(X.equals(tempCh)) break;
                else{
                    System.out.println("该句子包含未知字符");
                    return null;
                }
            } else if(M.get(X+":"+tempCh) != null){
                creatStr = M.get(X+":"+tempCh);
                String[] strings = M.get(X+":"+tempCh).split("");
                action.append("POP");
                if(!strings[3].equals("$")){
                    String tempStr = "";
                    for(int k = strings.length-1; k > 2; k--){
                        stack.push(strings[k]);
                        tempStr += strings[k];
                    }

                    action.append(", PUSH("+ tempStr +")");
                }

            }else if(M.get(X+":"+tempCh) == null){

                System.out.println(index + X+"无法推出"+ tempCh);
                return null;
            } else {
                System.out.println(X+tempCh);
                System.out.println("该句子不属于此文法");
                return null;
            }
            i++;
            //步骤
            rowOut.add(Integer.toString(i));

            //分析栈
            StringBuffer stringBuffer = new StringBuffer();
            stack.stream().forEach((String item)->stringBuffer.append(item));
            rowOut.add(stringBuffer.toString());

            //剩余输入串
            rowOut.add(string.substring(index));

            //所用产生式
            rowOut.add(creatStr);

            //动作
            rowOut.add(action.toString());

            outData.add(rowOut);

        }

        outData.stream().forEach((List<String> strList)->{
            String s ="";
            for (String str: strList){
                s = s + str + "     ";
            }
            System.out.println(s);
        });

        //将分析过程封装打包返回
        Data data = new Data();
        data.data = packageResult();
        data.M = getData();
        data.first = packageData(First);
        data.follow =packageData(Follow);
        return data;
    }

    /**
     * 读取下一个输入字符
     * @return
     */
    private String getNextChar(){

        return  String.valueOf(this.string.charAt(index));
    }

    /**
     * 初始化符号栈、输入字符串等
     * @param s
     */
    public void init(String s){
        this.string = s;
        System.out.println("输入句子为: "+s);
        stack.push("#");
    }

    /**
     * 初始化文法分析表
     */
    public void initM(){
        First = new HashMap<>();
        Follow = new HashMap<>();
        M = new HashMap<>();

        try {
            for(String s : VN){
                //获取所有非终结符的First集与Follow集
                First.put(s, getFirst(s));
                Follow.put(s, getFollow(s));
            }
        }catch (Exception e){
            System.out.println(First.toString());
            System.out.println(Follow.toString());
            return;
        }

        List<String> tempNT = new ArrayList<>();
        Arrays.stream(VT).forEach((String s)->tempNT.add(s));

        //将结束符#加入
        tempNT.add("#");

        for (String key : GS.keySet()) {
            List<String> strList = GS.get(key);
            for (String str : strList) {
                for(String s : tempNT){
                    if(!s.equals("$")){
                        List<String> tempFirst = getFirst(str);
                        //遍历终结符
                        if(tempFirst.contains(s)){
                            //若a在str的first集中
                            M.put(key+":"+s,key+"->"+str );
                        }

                        if(tempFirst.contains("$")){
                            //若str的first集包含空
                            if(Follow.get(key).contains(s)){
                                M.put(key+":"+s,key+"->"+str);
                            }
                        }
                    }
                }
            }
        }

        for(String k : M.keySet()){
            System.out.println(k + "        "+ M.get(k));
        }
    }

    /**
     * 求解first集合
     * @param gsChar 文法符号
     */
    public List<String> getFirst(String gsChar){
        List<String> firstOfGsChar = new ArrayList<>();
        boolean flag = true;
        int i;
        for(i = 0; i < gsChar.length(); i++){
            if (Util.isInArray(VT, String.valueOf(gsChar.charAt(i)))){
                //首字符即为终结符
                firstOfGsChar.add(String.valueOf(gsChar.charAt(i)));
                flag = false;
                break;
            }else {
                List<String> strList = GS.get(String.valueOf(gsChar.charAt(i)));
                List<String> tempFirst = new ArrayList<>();
                for(String str : strList){
                    List<String> tempList = getFirst(str);
                    try {
                        tempFirst = sumList(tempFirst, tempList);
                    }catch (Exception e){
                        System.out.println(e);
                        System.out.println(gsChar + tempFirst + tempList);
                        return null;
                    }
                }
                if(tempFirst.contains("$")){
                    //如果包含空字符
                    firstOfGsChar = addAllNotNullItem(firstOfGsChar, tempFirst);

                }else{
                    firstOfGsChar = sumList(firstOfGsChar, tempFirst);
                    flag = false;
                    break;
                }
            }
        }
        if (flag && i==gsChar.length()){
            //如果gsChar中全为非终结符，且全部可推出$, 则将$加入firstOfGsChar
            firstOfGsChar.add("$");
        }
        return firstOfGsChar;
    }

    /**
     * 求解follow集合
     */
    public List<String> getFollow(String gsChar){
        List<String> followOfGsChar = new ArrayList<>();
        if(gsChar.equals(startChar)){
            //如果是开始符，则将‘#’加入
            followOfGsChar.add("#");
        }
        for (String key : GS.keySet()){
            if (key.equals(gsChar))continue;
            List<String> strList = GS.get(key);
            for (String str:strList){
                //遍历文法中所有产生式
                if (str.contains(gsChar)){
                    int pos = str.indexOf(gsChar);
                    if(pos != -1){
                        //产生式中存在该非终结符
                        if (pos == str.length()-1 && !gsChar.equals(key)){
                            //在最后一个出现
                            followOfGsChar = sumList(followOfGsChar, getFollow(key));
                        } else if(!gsChar.equals(key)){
                            //在中间出现
                            String tempChar = str.substring(pos+1);

                            if(Util.isInArray(VT, tempChar)){
                                //其后一个字符是非终结符
                                followOfGsChar.add(tempChar);
                            }else {
                                //其后一个字符为非终结符
                                List<String> L = getFirst(tempChar);
                                followOfGsChar = addAllNotNullItem(followOfGsChar, L);
                                if(L.contains("$")&&!gsChar.equals(key)){
                                    //该非终结符能推出空,则把follow(key)集中的所以有元素加入
                                    followOfGsChar = sumList(followOfGsChar, getFollow(key));
                                }
                            }
                        }
                    }
                }
            }
        }

        return followOfGsChar;
    }

    /**
     * 读取文法文件
     */
    public void readGS(String filename) throws IOException {
        StringBuilder inString = new StringBuilder();
        InputStreamReader reader =
                new InputStreamReader(new FileInputStream(filename));
        for (int ch; (ch = reader.read()) != -1; ) {
            inString.append((char) ch);
        }

        String[] strings = inString.toString().split("\r\n");
//        Arrays.stream(strings).forEach((String s)-> System.out.println(s));
        GS = new HashMap<>();

        List<String> vtList = new ArrayList<>();

        Arrays.stream(strings).forEach((String s)->{
            String leftStr = s.substring(0, 1);
            String rightStr = s.substring(3);

            for (int i = 0; i < rightStr.length(); i++){
                //提取终结符
                if(!(rightStr.charAt(i) >= 'A' && rightStr.charAt(i) <= 'Z')){
                    vtList.add(String.valueOf(rightStr.charAt(i)));
                }
            }
//            System.out.println(rightStr+" "+leftStr);
            if(GS.containsKey(leftStr)){
                //如果该非终结符已经存在产生式, 则将后续产生式加入到其产生式表中
                GS.get(leftStr).add(rightStr);
            }else {
                ArrayList<String> strList = new ArrayList<>();
                strList.add(rightStr);
                GS.put(leftStr, strList);
            }
        });

        VN = GS.keySet().toArray(new String[0]);
        VT = vtList.stream().distinct().collect(Collectors.toList()).toArray(new String[0]);

//        for(String key: GS.keySet()){
//            GS.get(key).stream().forEach((String s)-> System.out.println(key+ "->" + s));
//        }

    }

    /**
     * 通过参数传递文法和待测句子
     */
    public void readGS(String gsData, String str){
        String[] strings = gsData.split("\n");
        //TODO 左递归情况处理

        //TODO 右回溯情况处理

        //存放经过处理的产生式集合
        List<String> newStrings = new ArrayList<>();
        //将带有或 | 运算符的产生式分解为一个或多个
        Arrays.stream(strings).forEach((String s)->{
            String leftStr = s.substring(0, 1);
            String rightStr = s.substring(3);

            String[] tempPtrings = {rightStr};

            if(rightStr.contains("|")){
                tempPtrings = rightStr.split("|");
            }

            for(String item:tempPtrings){
                newStrings.add(leftStr+"->"+item);
            }
        });


        GS = new HashMap<>();

        //临时存放终结符
        List<String> vtList = new ArrayList<>();

        newStrings.stream().forEach((String s)->{
            String leftStr = s.substring(0, 1);
            String rightStr = s.substring(3);

            for (int i = 0; i < rightStr.length(); i++){
                //提取终结符
                if(!(rightStr.charAt(i) >= 'A' && rightStr.charAt(i) <= 'Z')){
                    vtList.add(String.valueOf(rightStr.charAt(i)));
                }
            }
//            System.out.println(rightStr+" "+leftStr);
            if(GS.containsKey(leftStr)){
                //如果该非终结符已经存在产生式, 则将后续产生式加入到其产生式表中
                GS.get(leftStr).add(rightStr);
            }else {
                ArrayList<String> strList = new ArrayList<>();
                strList.add(rightStr);
                GS.put(leftStr, strList);
            }
        });


        init(str);

        //对终结符集与非终结符集赋值
        VN = GS.keySet().toArray(new String[0]);
        VT = vtList.stream().distinct().collect(Collectors.toList()).toArray(new String[0]);
    }

    /**
     * 消除文法左递归
     */
    private void cancelLeft(){

    }

    /**
     * 消除文法回溯
     */
    private void cancelReverse(){

    }

    /**
     * 将First(A)中所有非空元素加入到First(B)中
     * @param A
     * @param B
     */
    private List<String> addAllNotNullItem(List<String> A, List<String> B){
        A.stream().forEach((String s)->B.add(s));
        //去重与空字符$
        return B.stream().distinct().filter((String s)->!s.equals("$")).collect(Collectors.toList());
    }


    /**
     * 合并两集合元素并去重
     * @param A
     * @param B
     * @return
     */
    private List<String> sumList(List<String> A, List<String> B){
        A.forEach((String s)-> System.out.print(s));
        System.out.println(" ");
        B.forEach((String s)-> System.out.print(s));
        System.out.println(" ");
        try {
            A.stream().forEach((String s)->B.add(s));
        }catch (StackOverflowError e){
            System.out.println(e);
            A.forEach((String s)-> System.out.println(s));
            B.forEach((String s)-> System.out.println(s));
            return null;
        }
        return B.stream().distinct().collect(Collectors.toList());
    }


    /**
     * 封装
     * @param m
     * @return
     */
    private List<String> packageData(Map<String, List<String>> m){
        List<String> list = new ArrayList<>();
        for (String key:m.keySet()){
            String name = "First(" + key +") = {" ;
            for(String s : m.get(key)){
                name = name + s +" ";
            }
            name = name + "}";
            list.add(name);
        }
        return list;
    }

    /**
     * 封装分析信息数据
     * @return
     */
    private List<ResultData> packageResult(){
        return outData.stream().map((List<String> list)->{
            ResultData resultData = new ResultData();
            resultData.step = list.get(0);
            resultData.stackData =list.get(1);
            resultData.string = list.get(2);
            resultData.s = list.get(3);
            resultData.action = list.get(4);
            return resultData;
        }).collect(Collectors.toList());
    }

}
