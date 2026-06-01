package com.example.todo_api_v2.service;

import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderCreateRequest;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderLineResponse;
import com.example.todo_api_v2.dto.purchaseorder.PurchaseOrderResponse;
import com.example.todo_api_v2.entity.PurchaseOrder;
import com.example.todo_api_v2.entity.PurchaseOrderLine;
import com.example.todo_api_v2.exception.DuplicatePoNumberException;
import com.example.todo_api_v2.exception.PurchaseOrderNotFoundException;
import com.example.todo_api_v2.mapper.PurchaseOrderLineMapper;
import com.example.todo_api_v2.mapper.PurchaseOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseOrderService {

    // MapperをDI
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderLineMapper purchaseOrderLineMapper;

    /**
     * PurchaseOrder(ヘッダ)とPurchaseOrderLine(明細)を新規作成するメソッド
     *
     * @param request PurchaseOrderCreateRequest(DTO)
     * @return PurchaseOrderResponse
     */
    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderCreateRequest request){
        // requestをEntityに詰め替える
        String username = getCurrentUsername();
        PurchaseOrder newPurchaseOrder = convertPurchaseOrder(request, username);

        // PurcahseOrderをinsertする　→　poNumberはUKなので衝突した場合は独自例外でキャッチする
        try{
            purchaseOrderMapper.insert(newPurchaseOrder);
        }catch (DuplicateKeyException e){
            throw new DuplicatePoNumberException(
                    "poNumber:"+request.poNumber()+"は使用されています",e);
        }

        // poIdを取得
        Long newPoId = newPurchaseOrder.getId();

        // 明細リストに詰め替え
        List<PurchaseOrderLine> newPoLines = convertPurchaseOrderLines(request, newPoId, username);

        // 明細をinsert
        purchaseOrderLineMapper.insertLines(newPoLines);

        // respones作成
        PurchaseOrder po = purchaseOrderMapper.findById(newPoId).orElseThrow(()->
                new IllegalStateException("INSERT直後の発注ヘッダがDBに存在しません。poId="+ newPoId));
        List<PurchaseOrderLine> poLines = purchaseOrderLineMapper.findByPoId(newPoId);

        return assembleResponse(po, poLines);
    }

    /**
     * IDから発注ヘッダを検索。発注ヘッダと明細リストを返す
     *
     * @param id 発注ヘッダのid(PK)
     * @return PurchaseOrderResponse
     * @throws PurchaseOrderNotFoundException 指定したidの発注ヘッダが存在しない場合
     */
    public PurchaseOrderResponse findById(Long id){
        // DBから取得
        PurchaseOrder po = purchaseOrderMapper.findById(id).orElseThrow(()->
                new PurchaseOrderNotFoundException("発注ヘッダがDBに存在しません。poId="+ id));
        List<PurchaseOrderLine> poLines = purchaseOrderLineMapper.findByPoId(id);

        // response作成
        return assembleResponse(po, poLines);
    }

    /**
     * 発注ヘッダ+明細リストの組合せを全件返す
     *
     * @return List<PurchaseOrderResponse>
     */
    public List<PurchaseOrderResponse> findAll(){
        return purchaseOrderMapper.findAll().stream()
                .map( po-> assembleResponse(po, purchaseOrderLineMapper.findByPoId(po.getId()))
                ).toList();
    }// N+1だが発注件数が少ないため許容（実データでスロークエリが出たらIN句/JOINで最適化）

    /**
     * DTOをPurchaseOrderに詰めなおすヘルパーメソッド
     *
     * @param request DTO
     * @param username ログインユーザー
     * @return 詰め替えた後のEntity
     */
    private PurchaseOrder convertPurchaseOrder(PurchaseOrderCreateRequest request, String username){
        // orderDateがnull(未入力)の場合は今日の日付を入れる
        LocalDate orderDate = (request.orderDate() == null)? LocalDate.now():request.orderDate();
        return PurchaseOrder.createNew(
                request.poNumber(), request.supplier(), orderDate,username
        );
    }

    /**
     * DTOのlinesからList<PurchaseOrderLine>に詰めなおすヘルパーメソッド
     *
     * @param request リクエストDTO
     * @param newPoId 発注ヘッダのid
     * @param username ログインユーザー
     * @return 明細リスト(List<Entity>)
     */
    private List<PurchaseOrderLine> convertPurchaseOrderLines(PurchaseOrderCreateRequest request, Long newPoId, String username){
        // requestをPurchaseOrderLine 明細リストに詰め替え。lineNoの採番も同時に行う
        return IntStream.range(0, request.lines().size())
                .mapToObj(i->{
                    // i番目の明細DTOを取り出す。
                    var dtoLine = request.lines().get(i);

                    // Entityをここで生成
                    return PurchaseOrderLine.createNew(
                            newPoId,            // Poヘッダのid(FK)
                            dtoLine.itemId(),   // itemのid(FK)
                            i+1,                // lineNo(1行目から)
                            dtoLine.qty(),      // 数量
                            dtoLine.price(),    // 価格
                            dtoLine.dueDate(),  // 納期
                            username            // ログインユーザー
                    );

                })
                .toList();
    }

    /**
     * List<PurchaseOrderLineResponse>に詰め替えるヘルパーメソッド
     *
     * @param poLine DBから取得し直した明細Entityのリスト
     * @return 明細のresponseリスト
     */
    private List<PurchaseOrderLineResponse> convertPOLResponse(List<PurchaseOrderLine> poLine){
        return poLine.stream()
                .map(line -> new PurchaseOrderLineResponse(
                        line.getId(),
                        line.getPoId(),
                        line.getLineNo(),
                        line.getItemId(),
                        line.getQty(),
                        line.getPrice(),
                        line.getDueDate(),
                        line.getStatus(),
                        line.getReceivedBy(),
                        line.getReceivedAt(),
                        line.getCreatedBy(),
                        line.getCreatedAt(),
                        line.getUpdatedBy(),
                        line.getUpdatedAt()
                ))
                .toList();
    }

    /**
     * PurchaseOrderResponseに詰め替えるヘルパーメソッド
     *
     * @param po DBから取得し直した発注ヘッダEntity
     * @param responseLines 明細のresponseリスト(別で作成)
     * @return 発注ヘッダの情報
     */
    private PurchaseOrderResponse convertPOResponse(PurchaseOrder po, List<PurchaseOrderLineResponse> responseLines){
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier(),
                po.getOrderDate(),
                po.getStatus(),
                responseLines,
                po.getCreatedBy(),
                po.getCreatedAt(),
                po.getUpdatedBy(),
                po.getUpdatedAt()
        );
    }

    /**
     * responseの発注ヘッダ+明細リストを組み立てるヘルパーメソッド
     *
     * @param po 組み立て対象の発注ヘッダ
     * @return urchaseOrderResponse
     */
    private PurchaseOrderResponse assembleResponse(PurchaseOrder po, List<PurchaseOrderLine> lines){
        return convertPOResponse(po, convertPOLResponse(lines));
    }
    /**
     * 認証情報からusernameを読み取る。読み取れない場合はsystemとする
     *
     * @return username
     */
    private String getCurrentUsername(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
