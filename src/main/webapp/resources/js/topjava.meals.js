let mealsFilterOn;

// $(document).ready(function () {
$(function () {
    makeEditable({
            ajaxUrl: "ajax/profile/meals/",
            datatableApi: $("#datatable").DataTable({
                "paging": false,
                "info": true,
                "columns": [
                    {
                        "data": "dateTime"
                    },
                    {
                        "data": "description"
                    },
                    {
                        "data": "calories"
                    },
                    {
                        "defaultContent": "Edit",
                        "orderable": false
                    },
                    {
                        "defaultContent": "Delete",
                        "orderable": false
                    }
                ],
                "order": [
                    [
                        0,
                        "desc"
                    ]
                ]
            })
    });
    mealsFilterOn = false;
});

function filter() {
    mealsFilterOn = true;
    $.ajax({
        type: "GET",
        url: context.ajaxUrl + "filter",
        data: $('#filter').serialize()
    }).done(updateTableByData);
}

function clearFilter() {
    mealsFilterOn = false;
    $('#filter').find(":input").val("");
    updateTable();
}

function updateTable() {
    if (mealsFilterOn)
        filter();
    else
        updateTableCommon();
}