// $(document).ready(function () {
$(function () {
    makeEditable({
            ajaxUrl: "ajax/admin/users/",
            datatableApi: $("#datatable").DataTable({
                "paging": false,
                "info": true,
                "columns": [
                    {
                        "data": "name"
                    },
                    {
                        "data": "email"
                    },
                    {
                        "data": "roles"
                    },
                    {
                        "data": "enabled"
                    },
                    {
                        "data": "registered"
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
                        "asc"
                    ]
                ]
            })
        }
    );
});

function updateTable() {
    updateTableCommon();
}

function updateEnabled(checkbox) {
    const checked = checkbox.checked;
    let tr = $(checkbox.parentElement.parentElement);

    $.ajax({
        type: "POST",
        url: context.ajaxUrl + tr.attr("id"),
        data: "enabled=" + checked
    }).done(function() {
        tr.attr("data-userEnabled", checked);
        successNoty(checked ? "Enabled" : "Disabled");
    }).fail(function() {
        $(checkbox).prop('checked', !checked);
    });
}