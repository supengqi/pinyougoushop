package com.pinyougou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	
	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	@Autowired
	private TbSellerMapper sellerMappler;
	
	@Autowired
	private TbBrandMapper brandMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0"); // 状态未审核
		goodsMapper.insert(goods.getGoods());	// 插入商品基本信息	
		
		goods.getGoodsDesc().setGoodsId((goods.getGoods().getId()));  // 把商品基本表ID给拓展表
		goodsDescMapper.insert(goods.getGoodsDesc()); // 插入商品拓展表
		
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			
		
		for(TbItem item : goods.getItemList()) {
			// 构建标题SPU名称+规格选项值
			String title = goods.getGoods().getGoodsName();
			Map<String,Object> map = JSON.parseObject(item.getSpec());
			for(String key : map.keySet()) {
				title += " " + map.get(key);
				
			}
			item.setTitle(title); 
			setItemValue(goods,item);

			itemMapper.insert(item);
			}
		} else {   // 没有启用规格
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName()); // 标题
			item.setPrice(goods.getGoods().getPrice()); // 价格
			item.setNum(99998); // 库存
			item.setStatus("1"); // 状态
			item.setIsDefault("1");//是否默认
			item.setSpec("{}");
			
			setItemValue(goods,item);
			itemMapper.insert(item);
		}
	}

	private void setItemValue(Goods goods,TbItem item) {
					// 商品分类
					item.setCategoryid(goods.getGoods().getCategory3Id()); // 3级分类ID
					item.setCreateTime(new Date()); // 创建日期
					item.setUpdateTime(new Date()); // 更新日期
					item.setGoodsId(goods.getGoods().getId()); // 商品ID
					item.setSellerId(goods.getGoods().getSellerId()); // 卖家ID
					// 分类名称
					TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
					item.setCategory(itemCat.getName());
					// 品牌名称
					TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
					item.setBrand(brand.getName());
					
					// 商家名称
					TbSeller seller = sellerMappler.selectByPrimaryKey(goods.getGoods().getSellerId());
					item.setSeller(seller.getNickName());
					
					// 图片
					List<Map> imgList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
					if(imgList.size() > 0) {
						item.setImage(imgList.get(0).get("url").toString());
					}	
	}
	/**
	 * 修改
	 */
	@Override
	public void update(TbGoods goods){
		goodsMapper.updateByPrimaryKey(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbGoods findOne(Long id){
		return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
