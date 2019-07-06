 //控制层 
app.controller('goodsController' ,function($scope,$controller ,goodsService, uploadService,  itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	// 新增商品
	$scope.add=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		
		goodsService.add( $scope.entity ).success(
		function(response){
			if(response.success){
				alert('新增商品成功');
				$scope.entity={};
				editor.html(''); // 清空富文本编辑器
			} else {
				alert(response.message);
			}
		}
		);
	}
	
	// 上传图片
	$scope.uploadFile = function() {
		uploadService.uploadFile().success(
				function(response){
					if(response.success){
						$scope.image_entity.url= response.message;
					}else{
						alert(response.message);					
					}
				}		
			);
	}
	
	$scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}   };//定义页面实体结构
		//添加图片列表
		$scope.add_image_entity=function(){
			$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
			
	//列表中移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	//查询一级商品分类列表
	$scope.selectItemCat1List=function(){
	
		itemCatService.findByParentId(0).success(
			function(response){
				$scope.itemCat1List=response;			
			}
		);
		
	}
	
	//查询二级商品分类列表
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue) {
		// 根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(
				function(response) {
					$scope.itemCat2List=response;
				}
		);
		
	});
	
	//查询二级商品分类列表
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue) {
		// 根据选择的值，查询三级分类
		itemCatService.findByParentId(newValue).success(
				function(response) {
					$scope.itemCat3List=response;
				}
		);
		
	});
	
	
	// 三级分类选择后，读取模板ID
	$scope.$watch('entity.goods.category3Id',function(newValue,oleValue) {
		itemCatService.findOne(newValue).success(
				function(reponse) {
					$scope.entity.goods.typeTemplateId=reponse.typeId; // 更新模板ID
				}
		)
		
	});
	
	// 读取模板ID后，读取品牌下拉列表 拓展属性 规格列表
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oleValue) {
		typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplate=response; // 模板对象
					$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds); // 转JSON
					// 拓展属性
					$scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);//扩展属性
				
				}	
		);
		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(
				function(response) {
					$scope.specList=response;
				}
		)
		
	});
	
	$scope.updateSpecAttribute=function($event,name,value) {
		var object =  $scope.searchObjectByKey( $scope.entity.goodsDesc.specificationItems,'attributeName',name);
		// 原来有规格 则后面加到value
		if(object != null) {
			if($event.target.checked) {
				object.attributeValue.push(value);
			} else {
				object.attributeValue.splice(object.attributeValue.indexOf(value),1); // 移除选项
				if(object.attributeValue.length==0) {
					// 选项都取消了 将词条记录移除
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
			
		} else {
			// 没有规格 则添加到集合
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	
	// 创建SKU列表
	$scope.createItemList=function() {
		$scope.entity.itemList=[{ spec:{},price:0,num:99999,status:'0',isDefault:'0'  }]; // 列表初始化
		
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0;i <items.length;i++) {
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		
		}
	
	}
	
	// 定义一个循环方法  不再页面上调用 可以不加$scope
	addColumn=function(list,columnName,columnValues) {
		var newList=[];
		
		for(var i = 0; i < list.length;i++) {
			var oldRow = list[i];
			for (var j = 0; j < columnValues.length; j++) {
				var newRow = JSON.parse(JSON.stringify(oldRow)); // 深克隆
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		
		return newList;
	}
	
	
	
});	
