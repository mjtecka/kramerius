<div id="extendedMetadata" class="viewer">
    <div class="level0"><div class="meta"></div></div>
</div>

<script type="text/javascript">
    $(document).ready(function(){
        $('#extendedMetadata.viewer').bind('viewReady', function(event, viewerOptions){
            //updateMetadata(viewerOptions.fullid);
        });
        $('#extendedMetadata.viewer').bind('selectedPathChanged', function(event, level){
            updateMetaFromLevel(level);
        });
    });

    function updateMetaFromLevel(level){
        
        var uuid = "";
        var model;
        var models;
        for(var i=level; i<k4Settings.selectedPath.length; i++){
            if($("#extendedMetadata div.level"+(i+1)).length==0)
                $("#extendedMetadata div.level"+(i)).append('<div class="level'+(i+1)+'"><div class="meta"></div></div>');
        }
        for(var i=0; i<k4Settings.selectedPath.length; i++){
            uuid = k4Settings.selectedPath[i].split('_')[1];
            models = k4Settings.selectedPath[i].split('_')[0].split('-');
            model = models[models.length-1];
            getModelMetadata(uuid, i, model);
        }
    }
   
    function updateMetadata(){
        var uuid = "";
        var model;
        var models;
        //$("#extendedMetadata div.level0").html('<div class="meta"></div>');
        for(var i=0; i<k4Settings.selectedPath.length; i++){
            $("#extendedMetadata div.level"+(i)).append('<div class="level'+(i+1)+'"><div class="meta"></div></div>');
            //    $('#test').append(k4Settings.selectedPath[i] + '<br/>');
        }
        for(var i=0; i<k4Settings.selectedPath.length; i++){
            uuid = k4Settings.selectedPath[i].split('_')[1];
            models = k4Settings.selectedPath[i].split('_')[0].split('-');
            model = models[models.length-1];
            getModelMetadata(uuid, i, model);
        }
    }

    function getModelMetadata(pid, level, model){
        var url = "inc/details/metadata.jsp?pid=" + pid + "&model=" + model + "&level=" + level;
        $.get(url, function(data){
            $("#extendedMetadata div.level"+level+">div.meta").html(data);
        }); 
    }
 
</script>