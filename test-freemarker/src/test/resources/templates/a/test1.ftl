<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf‐8">
        <title>Hello World!</title>
    </head>
    <body>
        Hello ${name}!
        <br>
        遍历模型中的学生信息(模型中的变量名为list)
        <hr>
        <table>
            <tr>
                <td>序号</td>
                <td>姓名</td>
                <td>年龄</td>
                <td>金额</td>
                <td>出生日期</td>
            </tr>
            <#--判断对象是否为空-->
            <#if stus??>
                <#list stus as stu>
                    <tr>
                        <td>${stu_index + 1}</td>
                        <#--if的使用方法-->
                        <td <#if stu.name=="小明">style="background-color: red"</#if> >${stu.name}</td>
                        <td>${stu.age}</td>
                        <#--运算符的使用-->
                        <td <#if (stu.mondy>300)>style="background: red"</#if> >${stu.mondy}</td>
                        <#--dirthday是日期类型,所以添加date-->
                        <#--<td>${stu.birthday?date}</td>-->
                        <#--自定义年月日-->
                        <td>${stu.birthday?string("YYYY年MM月dd日")}</td>
                    </tr>
                </#list>
                <br>
                <#--查看元素的数量-->
                学生的个数: ${stus?size}
            </#if>
        </table>
        <hr>
        遍历数据模型中的stuMap()<br>

        <#--在中括号中添加key-->
        姓名:${(stuMap['stu1'].name)!''}<br>
        年龄:${(stuMap['stu1'].age)!''}<br>

        <#--通过map加 . 的方式-->
        <#--如果有为空的情况就显示 空字符串-->
        姓名:${(stuMap.stu1.name)!''}<br>
        年龄:${(stuMap.stu1.age)!''}<br>
        <#--遍历map中的key 获取中map中所有的key,然后赋值给 k对象-->
        <#list stuMap?keys as k>
            姓名:${stuMap[k].name}<br>
            年龄:${stuMap[k].age}<br>
        </#list>
        <hr>
        <#--数字类型转换为字符串-->
        ${point?c}
        <hr>
        <#--json字符串转换为对象,eval表示把字符串转换为对象-->
        <#assign text="{'bank':'工商银行','account':'10101920201920212'}" /><#assign data=text?eval />
        开户行：${data.bank} 账号：${data.account}
    </body>
</html>