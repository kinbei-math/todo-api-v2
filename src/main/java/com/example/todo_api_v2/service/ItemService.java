package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.ItemCreateRequest;
import com.example.todo_api_v2.dto.ItemResponse;
import com.example.todo_api_v2.entity.Item;
import com.example.todo_api_v2.exception.DuplicateItemCodeException;
import com.example.todo_api_v2.exception.ItemNotFoundException;
import com.example.todo_api_v2.mapper.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j // ログメッセージ用
public class ItemService {

    // ItemMapperをDI
    private final ItemMapper itemMapper;
    public ItemService(ItemMapper itemMapper){this.itemMapper = itemMapper;}

    // 1. IDで品目を取得
    public ItemResponse findById(Long id){
        return itemMapper.findById(id)
                .map(this::convertItemResponse)
                .orElseThrow(()-> new ItemNotFoundException("品目が見つかりません。id=" + id));
    }

    // 2. itemの一覧を取得
    public List<ItemResponse> findAll(){
        return itemMapper.findAll().stream()
                .map(this::convertItemResponse)
                .toList();
    }

    // 3. 新規登録
    @Transactional // 登録の途中でエラーが起きても変なデータが登録されないようにする。
    public ItemResponse createItem(ItemCreateRequest request){
        // itemCodeの重複チェック(itemCodeでの検索結果があれば重複エラー)
        if(itemMapper.findByItemCode(request.itemCode()).isPresent()){
            throw new DuplicateItemCodeException(
                    "品目コードが既に存在します。itemCode=" + request.itemCode()
            );
        }

        // Insert用のItemクラスを用意
        Item item = new Item();
        item.setItemCode(request.itemCode());
        item.setName(request.name());
        item.setUom(request.uom());
        item.setCategory(request.category());

        // Insert実行(SQL)
        itemMapper.insert(item);

        // ログを出力
        log.info("Item created [UserID: {}, ItemId: {}, ItemCode: {}, Name: {}]",
                getCurrentUsername(), item.getId(), item.getItemCode(), item.getName());

        // 出力用に詰め替え
        return convertItemResponse(item);
    }


    // Responseに移し替えるメソッド
    private ItemResponse convertItemResponse(Item item){
        return new ItemResponse(
                item.getId(), item.getItemCode(), item.getName(), item.getUom(), item.getCategory(),
                item.getCreatedAt(),item.getUpdatedAt()
        );
    }

    private String getCurrentUsername(){
        // 認証情報を取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }

}
