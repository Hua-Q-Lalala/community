$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

//点赞
function like(btn, entityType, entityId, entityUserId, postId){

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 0){
                //通过当前点赞的a标签对象，获取子标签<i>和<b>
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            } else{
                alert(data.msg);
            }
        }
    );

}

//置顶
function setTop(){
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                // $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
                $("#topBtn").attr("disabled", "disabled").text("已置顶").removeClass("btn-danger").addClass("btn-info");
            }else{
                alert(data.msg);
            }
        }
    );
}

//加精
function setWonderful(){
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#wonderfulBtn").attr("disabled", "disabled").text("已加精").removeClass("btn-danger").addClass("btn-info");
            }else{
                alert(data.msg);
            }
        }
    );
}

//删除
function setDelete(){
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                alert(data.msg);
                //2秒后，自动隐藏提示框
                setTimeout(function(){
                    location.href = CONTEXT_PATH + "/index";
                }, 2000);

            }else{
                alert(data.msg);
            }
        }
    );
}