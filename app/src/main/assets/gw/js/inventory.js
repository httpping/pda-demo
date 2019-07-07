$.ajax({
    type:'get',
    url:'json/tableData-1.json',
    dataType: "json",
    data: {},
    success: function (data) {
        var html = template("template-mod",data);

        $('#tableData').append(html);
        operating();
    }
});
//表格操作
function operating() {
    $("#tableOperating").on('click', '.del', function () {
        //删除当前表格行
        $(this).closest('tr').remove();
        //循环表格重排序号
        $('#tableOperating table tbody tr').each(function () {
            var num = $(this).index() + 1;
            $(this).find('td:eq(0)').text(num)
        })
    });
}