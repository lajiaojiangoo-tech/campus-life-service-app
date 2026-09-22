"""
学生生活服务管理 - Python Web服务端
基于Flask框架，使用JSON文件存取数据
提供5个功能模块API：用户认证、快递服务、饮食订餐、热水服务、打印服务、上网服务
"""

import json
import os
import uuid
from datetime import datetime

from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'data')
os.makedirs(DATA_DIR, exist_ok=True)


# ========== JSON文件读写工具 ==========

def load_json(filename):
    filepath = os.path.join(DATA_DIR, filename)
    if not os.path.exists(filepath):
        return []
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)


def save_json(filename, data):
    filepath = os.path.join(DATA_DIR, filename)
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def init_data():
    """初始化默认数据"""
    # 用户数据
    if not os.path.exists(os.path.join(DATA_DIR, 'users.json')):
        users = [
            {"id": "1", "username": "admin", "password": "123456",
             "name": "马铭涛", "studentId": "24103230119", "dorm": "1栋-305"}
        ]
        save_json('users.json', users)

    # 快递数据
    if not os.path.exists(os.path.join(DATA_DIR, 'express.json')):
        expresses = [
            {"id": "1", "userId": "1", "trackingNo": "SF1234567890", "company": "顺丰",
             "status": "待取件", "pickupCode": "A1234", "arrivalTime": "06-18 14:30"},
            {"id": "2", "userId": "1", "trackingNo": "ZTO8888888888", "company": "中通",
             "status": "运输中", "pickupCode": "B5678", "arrivalTime": ""},
            {"id": "3", "userId": "1", "trackingNo": "YTO6666666666", "company": "圆通",
             "status": "已签收", "pickupCode": "C9012", "arrivalTime": "06-15 09:20"},
            {"id": "4", "userId": "1", "trackingNo": "YD9999999999", "company": "韵达",
             "status": "待取件", "pickupCode": "D3456", "arrivalTime": "06-19 10:00"},
        ]
        save_json('express.json', expresses)

    # 菜品数据
    if not os.path.exists(os.path.join(DATA_DIR, 'foods.json')):
        foods = [
            {"id": "1", "name": "宫保鸡丁", "desc": "微辣 · 配米饭", "price": 12},
            {"id": "2", "name": "红烧排骨", "desc": "香浓 · 配青菜", "price": 16},
            {"id": "3", "name": "番茄鸡蛋", "desc": "清淡 · 经典家常", "price": 10},
            {"id": "4", "name": "牛肉面", "desc": "加面可选", "price": 15},
            {"id": "5", "name": "酸辣土豆丝", "desc": "开胃小炒", "price": 8},
            {"id": "6", "name": "鱼香肉丝", "desc": "甜辣 · 下饭神器", "price": 14},
            {"id": "7", "name": "麻婆豆腐", "desc": "麻辣 · 配米饭", "price": 10},
            {"id": "8", "name": "清炒时蔬", "desc": "清淡 · 健康之选", "price": 8},
        ]
        save_json('foods.json', foods)

    # 订单数据
    if not os.path.exists(os.path.join(DATA_DIR, 'orders.json')):
        save_json('orders.json', [])

    # 热水账户数据
    if not os.path.exists(os.path.join(DATA_DIR, 'hotwater.json')):
        accounts = [
            {"userId": "1", "balance": 50.0}
        ]
        save_json('hotwater.json', accounts)

    # 热水使用记录
    if not os.path.exists(os.path.join(DATA_DIR, 'hotwater_records.json')):
        save_json('hotwater_records.json', [])

    # 打印记录数据
    if not os.path.exists(os.path.join(DATA_DIR, 'print_records.json')):
        save_json('print_records.json', [])

    # 钱包数据
    if not os.path.exists(os.path.join(DATA_DIR, 'wallet.json')):
        wallets = [
            {"userId": "1", "balance": 100.0}
        ]
        save_json('wallet.json', wallets)

    # 钱包交易记录
    if not os.path.exists(os.path.join(DATA_DIR, 'wallet_records.json')):
        save_json('wallet_records.json', [])

    # 上网账户数据
    if not os.path.exists(os.path.join(DATA_DIR, 'internet.json')):
        accounts = [
            {"userId": "1", "balance": 30.0, "flowGB": 20.0, "isLogin": False}
        ]
        save_json('internet.json', accounts)


init_data()


# ========== 模块1：用户认证 ==========

@app.route('/api/login', methods=['POST'])
def login():
    """用户登录"""
    data = request.get_json()
    username = data.get('username', '')
    password = data.get('password', '')

    users = load_json('users.json')
    for user in users:
        if user['username'] == username and user['password'] == password:
            return jsonify({
                "code": 200,
                "msg": "登录成功",
                "data": {
                    "userId": user['id'],
                    "username": user['username'],
                    "name": user['name'],
                    "studentId": user['studentId'],
                    "dorm": user['dorm']
                }
            })

    return jsonify({"code": 401, "msg": "用户名或密码错误", "data": None})


@app.route('/api/register', methods=['POST'])
def register():
    """用户注册"""
    data = request.get_json()
    username = data.get('username', '')
    password = data.get('password', '')
    name = data.get('name', '')
    student_id = data.get('studentId', '')
    dorm = data.get('dorm', '')

    users = load_json('users.json')

    for user in users:
        if user['username'] == username:
            return jsonify({"code": 400, "msg": "用户名已存在", "data": None})

    new_user = {
        "id": str(uuid.uuid4())[:8],
        "username": username,
        "password": password,
        "name": name,
        "studentId": student_id,
        "dorm": dorm
    }
    users.append(new_user)
    save_json('users.json', users)

    # 同时创建热水和上网账户
    hotwater = load_json('hotwater.json')
    hotwater.append({"userId": new_user['id'], "balance": 50.0})
    save_json('hotwater.json', hotwater)

    internet = load_json('internet.json')
    internet.append({"userId": new_user['id'], "balance": 30.0, "flowGB": 20.0, "isLogin": False})
    save_json('internet.json', internet)

    # 创建钱包账户
    wallet = load_json('wallet.json')
    wallet.append({"userId": new_user['id'], "balance": 100.0})
    save_json('wallet.json', wallet)

    return jsonify({"code": 200, "msg": "注册成功", "data": {"userId": new_user['id']}})


@app.route('/api/user/<user_id>', methods=['GET'])
def get_user(user_id):
    """获取用户信息"""
    users = load_json('users.json')
    for user in users:
        if user['id'] == user_id:
            return jsonify({
                "code": 200,
                "msg": "获取成功",
                "data": {
                    "userId": user['id'],
                    "username": user['username'],
                    "name": user['name'],
                    "studentId": user['studentId'],
                    "dorm": user['dorm']
                }
            })
    return jsonify({"code": 404, "msg": "用户不存在", "data": None})


@app.route('/api/user/<user_id>', methods=['PUT'])
def update_user(user_id):
    """更新用户信息"""
    data = request.get_json()
    users = load_json('users.json')

    for user in users:
        if user['id'] == user_id:
            if 'name' in data:
                user['name'] = data['name']
            if 'studentId' in data:
                user['studentId'] = data['studentId']
            if 'dorm' in data:
                user['dorm'] = data['dorm']
            save_json('users.json', users)
            return jsonify({"code": 200, "msg": "更新成功", "data": None})

    return jsonify({"code": 404, "msg": "用户不存在", "data": None})


# ========== 模块1.5：钱包服务 ==========

def deduct_wallet(user_id, amount, desc):
    """从钱包扣款，返回(是否成功, 余额/错误信息)"""
    wallets = load_json('wallet.json')
    for w in wallets:
        if w['userId'] == user_id:
            if w['balance'] < amount:
                return False, "钱包余额不足，请先充值"
            w['balance'] = round(w['balance'] - amount, 2)
            save_json('wallet.json', wallets)

            # 记录交易
            records = load_json('wallet_records.json')
            records.insert(0, {
                "id": str(uuid.uuid4())[:8],
                "userId": user_id,
                "type": "支出",
                "amount": amount,
                "balance": w['balance'],
                "desc": desc,
                "time": datetime.now().strftime("%Y-%m-%d %H:%M")
            })
            save_json('wallet_records.json', records)
            return True, w['balance']
    return False, "钱包账户不存在"


@app.route('/api/wallet/balance/<user_id>', methods=['GET'])
def get_wallet_balance(user_id):
    """查询钱包余额"""
    wallets = load_json('wallet.json')
    for w in wallets:
        if w['userId'] == user_id:
            return jsonify({"code": 200, "msg": "查询成功", "data": {"balance": w['balance']}})
    return jsonify({"code": 404, "msg": "钱包不存在", "data": None})


@app.route('/api/wallet/recharge', methods=['POST'])
def recharge_wallet():
    """钱包充值"""
    data = request.get_json()
    user_id = data.get('userId', '')
    amount = data.get('amount', 0)

    if amount <= 0:
        return jsonify({"code": 400, "msg": "充值金额必须大于0", "data": None})

    wallets = load_json('wallet.json')
    for w in wallets:
        if w['userId'] == user_id:
            w['balance'] = round(w['balance'] + amount, 2)
            save_json('wallet.json', wallets)

            # 记录交易
            records = load_json('wallet_records.json')
            records.insert(0, {
                "id": str(uuid.uuid4())[:8],
                "userId": user_id,
                "type": "充值",
                "amount": amount,
                "balance": w['balance'],
                "desc": "钱包充值",
                "time": datetime.now().strftime("%Y-%m-%d %H:%M")
            })
            save_json('wallet_records.json', records)

            return jsonify({"code": 200, "msg": "充值成功", "data": {"balance": w['balance']}})

    return jsonify({"code": 404, "msg": "钱包不存在", "data": None})


@app.route('/api/wallet/records/<user_id>', methods=['GET'])
def wallet_records(user_id):
    """获取钱包交易记录"""
    records = load_json('wallet_records.json')
    user_records = [r for r in records if r['userId'] == user_id]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_records})


# ========== 模块2：快递服务 ==========

@app.route('/api/express/query', methods=['GET'])
def query_express():
    """根据单号查询快递"""
    tracking_no = request.args.get('trackingNo', '')

    expresses = load_json('express.json')
    for exp in expresses:
        if exp['trackingNo'].lower() == tracking_no.lower():
            return jsonify({"code": 200, "msg": "查询成功", "data": exp})

    return jsonify({"code": 404, "msg": "未找到该快递记录", "data": None})


@app.route('/api/express/list/<user_id>', methods=['GET'])
def list_express(user_id):
    """获取用户快递列表"""
    expresses = load_json('express.json')
    user_list = [e for e in expresses if e['userId'] == user_id]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_list})


@app.route('/api/express/pickup', methods=['POST'])
def pickup_express():
    """取件确认 - 校验用户权限"""
    data = request.get_json()
    express_id = data.get('expressId', '')
    user_id = data.get('userId', '')

    expresses = load_json('express.json')
    for exp in expresses:
        if exp['id'] == express_id:
            if exp['userId'] != user_id:
                return jsonify({"code": 403, "msg": "无权操作：该快递不属于您", "data": None})
            exp['status'] = '已签收'
            save_json('express.json', expresses)
            return jsonify({"code": 200, "msg": "取件成功", "data": exp})

    return jsonify({"code": 404, "msg": "快递不存在", "data": None})


@app.route('/api/express/send', methods=['POST'])
def send_express():
    """提交寄件请求"""
    data = request.get_json()
    user_id = data.get('userId', '')
    receiver = data.get('receiver', '')
    phone = data.get('phone', '')
    address = data.get('address', '')
    item_type = data.get('itemType', '')

    if not receiver or not phone or not address:
        return jsonify({"code": 400, "msg": "请填写完整信息", "data": None})

    record = {
        "id": str(uuid.uuid4())[:8],
        "userId": user_id,
        "receiver": receiver,
        "phone": phone,
        "address": address,
        "itemType": item_type,
        "status": "待揽件",
        "time": datetime.now().strftime("%Y-%m-%d %H:%M")
    }

    sends = load_json('sends.json')
    sends.insert(0, record)
    save_json('sends.json', sends)

    return jsonify({"code": 200, "msg": "寄件提交成功", "data": record})


# ========== 模块3：饮食订餐 ==========

@app.route('/api/food/list', methods=['GET'])
def list_foods():
    """获取菜品列表"""
    foods = load_json('foods.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": foods})


@app.route('/api/order/submit', methods=['POST'])
def submit_order():
    """提交订单"""
    data = request.get_json()
    user_id = data.get('userId', '')
    items = data.get('items', [])

    if not items:
        return jsonify({"code": 400, "msg": "订单不能为空", "data": None})

    total = 0
    order_items = []
    foods = load_json('foods.json')

    for item in items:
        food_id = item.get('foodId', '')
        count = item.get('count', 0)
        if count <= 0:
            continue

        food = next((f for f in foods if f['id'] == food_id), None)
        if food:
            subtotal = food['price'] * count
            total += subtotal
            order_items.append({
                "foodId": food_id,
                "name": food['name'],
                "price": food['price'],
                "count": count,
                "subtotal": subtotal
            })

    if not order_items:
        return jsonify({"code": 400, "msg": "订单不能为空", "data": None})

    # 扣除钱包余额
    ok, result = deduct_wallet(user_id, total, "订餐消费")
    if not ok:
        return jsonify({"code": 400, "msg": result, "data": None})
    wallet_balance = result

    order = {
        "id": str(uuid.uuid4())[:8],
        "userId": user_id,
        "items": order_items,
        "total": total,
        "status": "已提交",
        "time": datetime.now().strftime("%Y-%m-%d %H:%M")
    }

    orders = load_json('orders.json')
    orders.insert(0, order)
    save_json('orders.json', orders)

    return jsonify({"code": 200, "msg": "下单成功", "data": {**order, "walletBalance": wallet_balance}})


@app.route('/api/order/list/<user_id>', methods=['GET'])
def list_orders(user_id):
    """获取用户订单列表"""
    orders = load_json('orders.json')
    user_orders = [o for o in orders if o['userId'] == user_id]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_orders})


# ========== 模块4：热水服务 ==========

@app.route('/api/hotwater/balance/<user_id>', methods=['GET'])
def get_hotwater_balance(user_id):
    """查询热水余额"""
    accounts = load_json('hotwater.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            return jsonify({"code": 200, "msg": "查询成功", "data": {"balance": acc['balance']}})

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/hotwater/recharge', methods=['POST'])
def recharge_hotwater():
    """热水充值"""
    data = request.get_json()
    user_id = data.get('userId', '')
    amount = data.get('amount', 0)

    if amount <= 0:
        return jsonify({"code": 400, "msg": "充值金额必须大于0", "data": None})

    accounts = load_json('hotwater.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            acc['balance'] += amount
            save_json('hotwater.json', accounts)

            # 记录充值
            records = load_json('hotwater_records.json')
            records.insert(0, {
                "id": str(uuid.uuid4())[:8],
                "userId": user_id,
                "type": "充值",
                "amount": amount,
                "balance": acc['balance'],
                "time": datetime.now().strftime("%Y-%m-%d %H:%M")
            })
            save_json('hotwater_records.json', records)

            return jsonify({"code": 200, "msg": "充值成功", "data": {"balance": acc['balance']}})

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/hotwater/use', methods=['POST'])
def use_hotwater():
    """使用热水 - 扣钱包余额"""
    data = request.get_json()
    user_id = data.get('userId', '')
    liters = data.get('liters', 0)
    location = data.get('location', '')

    if liters <= 0:
        return jsonify({"code": 400, "msg": "出水量必须大于0", "data": None})

    cost = liters * 1.0

    # 扣除钱包余额
    ok, result = deduct_wallet(user_id, cost, f"热水消费 {liters}L")
    if not ok:
        return jsonify({"code": 400, "msg": result, "data": None})

    # 记录使用
    records = load_json('hotwater_records.json')
    records.insert(0, {
        "id": str(uuid.uuid4())[:8],
        "userId": user_id,
        "type": "使用",
        "liters": liters,
        "cost": cost,
        "location": location,
        "balance": result,
        "time": datetime.now().strftime("%Y-%m-%d %H:%M")
    })
    save_json('hotwater_records.json', records)

    return jsonify({
        "code": 200,
        "msg": "出水成功",
        "data": {"cost": cost, "balance": result, "location": location, "liters": liters}
    })


@app.route('/api/hotwater/records/<user_id>', methods=['GET'])
def hotwater_records(user_id):
    """获取热水使用记录"""
    records = load_json('hotwater_records.json')
    user_records = [r for r in records if r['userId'] == user_id]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_records})


# ========== 模块5：打印服务 ==========

@app.route('/api/print/submit', methods=['POST'])
def submit_print():
    """提交打印任务 - 扣钱包余额"""
    data = request.get_json()
    user_id = data.get('userId', '')
    file_name = data.get('fileName', '')
    paper = data.get('paper', 'A4')
    color_type = data.get('colorType', '黑白')
    side = data.get('side', '单面')
    copies = data.get('copies', 1)

    if not file_name:
        return jsonify({"code": 400, "msg": "请选择文件", "data": None})

    # 计算价格
    unit_price = 0.5 if color_type == '黑白' else 1.5
    if paper == 'A3':
        unit_price *= 1.5
    if side == '双面':
        unit_price *= 0.8
    total_price = round(unit_price * copies, 2)

    # 扣除钱包余额
    ok, result = deduct_wallet(user_id, total_price, f"打印消费 {file_name}")
    if not ok:
        return jsonify({"code": 400, "msg": result, "data": None})

    record = {
        "id": str(uuid.uuid4())[:8],
        "userId": user_id,
        "fileName": file_name,
        "paper": paper,
        "colorType": color_type,
        "side": side,
        "copies": copies,
        "price": total_price,
        "status": "已提交",
        "time": datetime.now().strftime("%Y-%m-%d %H:%M")
    }

    records = load_json('print_records.json')
    records.insert(0, record)
    save_json('print_records.json', records)

    return jsonify({"code": 200, "msg": "提交打印成功", "data": {**record, "walletBalance": result}})


@app.route('/api/print/records/<user_id>', methods=['GET'])
def print_records(user_id):
    """获取打印记录"""
    records = load_json('print_records.json')
    user_records = [r for r in records if r['userId'] == user_id]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_records})


@app.route('/api/print/cancel', methods=['POST'])
def cancel_print():
    """取消打印任务"""
    data = request.get_json()
    record_id = data.get('recordId', '')

    records = load_json('print_records.json')
    for rec in records:
        if rec['id'] == record_id:
            rec['status'] = '已取消'
            save_json('print_records.json', records)
            return jsonify({"code": 200, "msg": "取消成功", "data": None})

    return jsonify({"code": 404, "msg": "记录不存在", "data": None})


# ========== 模块6：上网服务 ==========

@app.route('/api/internet/status/<user_id>', methods=['GET'])
def get_internet_status(user_id):
    """查询上网状态"""
    accounts = load_json('internet.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            return jsonify({
                "code": 200,
                "msg": "查询成功",
                "data": {
                    "isLogin": acc['isLogin'],
                    "balance": acc['balance'],
                    "flowGB": acc['flowGB']
                }
            })

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/internet/login', methods=['POST'])
def internet_login():
    """校园网登录"""
    data = request.get_json()
    user_id = data.get('userId', '')

    accounts = load_json('internet.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            acc['isLogin'] = True
            save_json('internet.json', accounts)
            return jsonify({"code": 200, "msg": "登录成功", "data": {"isLogin": True}})

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/internet/logout', methods=['POST'])
def internet_logout():
    """校园网登出"""
    data = request.get_json()
    user_id = data.get('userId', '')

    accounts = load_json('internet.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            acc['isLogin'] = False
            save_json('internet.json', accounts)
            return jsonify({"code": 200, "msg": "已登出", "data": {"isLogin": False}})

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/internet/recharge', methods=['POST'])
def internet_recharge():
    """上网费充值 - 从钱包扣款"""
    data = request.get_json()
    user_id = data.get('userId', '')
    amount = data.get('amount', 0)

    if amount <= 0:
        return jsonify({"code": 400, "msg": "充值金额必须大于0", "data": None})

    # 从钱包扣款
    ok, result = deduct_wallet(user_id, amount, "上网费充值")
    if not ok:
        return jsonify({"code": 400, "msg": result, "data": None})

    # 上网账户余额增加
    accounts = load_json('internet.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            acc['balance'] += amount
            acc['balance'] = round(acc['balance'], 2)
            save_json('internet.json', accounts)
            return jsonify({
                "code": 200,
                "msg": "充值成功",
                "data": {"balance": acc['balance'], "flowGB": acc['flowGB'], "walletBalance": result}
            })

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/internet/query_flow', methods=['POST'])
def query_flow():
    """查询流量"""
    data = request.get_json()
    user_id = data.get('userId', '')

    accounts = load_json('internet.json')
    for acc in accounts:
        if acc['userId'] == user_id:
            return jsonify({
                "code": 200,
                "msg": "查询成功",
                "data": {"flowGB": acc['flowGB'], "balance": acc['balance']}
            })

    return jsonify({"code": 404, "msg": "账户不存在", "data": None})


@app.route('/api/internet/records/<user_id>', methods=['GET'])
def internet_records(user_id):
    """获取上网充值记录"""
    records = load_json('wallet_records.json')
    user_records = [r for r in records if r['userId'] == user_id and '上网' in r.get('desc', '')]
    return jsonify({"code": 200, "msg": "获取成功", "data": user_records})


# ========== Web管理页面 ==========

@app.route('/')
def index():
    """Web管理首页 - 展示所有数据表和API操作"""
    return '''
<!DOCTYPE html>
<html lang="zh">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>学生生活服务管理 - Web服务端</title>
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: "Microsoft YaHei", sans-serif; background: #f5f5f5; color: #333; }
.header { background: linear-gradient(135deg, #4A90D9, #357ABD); color: white; padding: 24px; text-align: center; }
.header h1 { font-size: 24px; margin-bottom: 8px; }
.header p { opacity: 0.85; font-size: 14px; }
.container { max-width: 1200px; margin: 20px auto; padding: 0 16px; }
.section { background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); margin-bottom: 20px; overflow: hidden; }
.section-title { background: #f8f9fa; padding: 14px 20px; font-size: 16px; font-weight: bold; border-bottom: 1px solid #eee; display: flex; align-items: center; gap: 8px; }
.section-title .badge { background: #4A90D9; color: white; font-size: 12px; padding: 2px 8px; border-radius: 10px; }
.section-body { padding: 16px 20px; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th { background: #4A90D9; color: white; padding: 10px 12px; text-align: left; font-weight: normal; }
td { padding: 8px 12px; border-bottom: 1px solid #f0f0f0; }
tr:hover td { background: #f8faff; }
.api-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 12px; }
.api-card { border: 1px solid #e8e8e8; border-radius: 8px; padding: 14px; }
.api-card .method { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; margin-right: 6px; }
.method-get { background: #e8f5e9; color: #2e7d32; }
.method-post { background: #e3f2fd; color: #1565c0; }
.method-put { background: #fff3e0; color: #e65100; }
.api-card .path { font-family: monospace; font-size: 13px; }
.api-card .desc { color: #888; font-size: 12px; margin-top: 6px; }
.test-area { margin-top: 12px; padding-top: 12px; border-top: 1px dashed #eee; }
.test-area label { font-size: 12px; color: #666; display: block; margin-bottom: 4px; }
.test-area input, .test-area select { width: 100%; padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; margin-bottom: 8px; }
.test-area button { background: #4A90D9; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; font-size: 13px; }
.test-area button:hover { background: #357ABD; }
.result { margin-top: 10px; background: #1e1e1e; color: #d4d4d4; padding: 12px; border-radius: 8px; font-family: monospace; font-size: 12px; white-space: pre-wrap; max-height: 300px; overflow-y: auto; display: none; }
.tabs { display: flex; border-bottom: 2px solid #eee; }
.tab { padding: 10px 20px; cursor: pointer; font-size: 14px; border-bottom: 2px solid transparent; margin-bottom: -2px; }
.tab.active { color: #4A90D9; border-bottom-color: #4A90D9; font-weight: bold; }
.tab-content { display: none; }
.tab-content.active { display: block; }
.empty { text-align: center; color: #999; padding: 20px; }
</style>
</head>
<body>
<div class="header">
    <h1>学生生活服务管理 - Web服务端</h1>
    <p>Flask + JSON文件存储 | 数据库表结构与API接口管理</p>
</div>
<div class="container">

<!-- 注册与登录 -->
<div class="section">
    <div class="section-title">模块1：用户认证（注册/登录）<span class="badge">users.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'reg-tab')">注册</div>
            <div class="tab" onclick="switchTab(this, 'login-tab')">登录</div>
            <div class="tab" onclick="switchTab(this, 'users-tab')">用户数据表</div>
        </div>
        <div id="reg-tab" class="tab-content active">
            <div class="test-area">
                <label>用户名</label><input id="reg-username" value="testuser">
                <label>密码</label><input id="reg-password" type="password" value="123456">
                <label>姓名</label><input id="reg-name" value="测试用户">
                <label>学号</label><input id="reg-sid" value="24103230199">
                <label>宿舍</label><input id="reg-dorm" value="3栋-201">
                <button onclick="doRegister()">注册</button>
                <div id="reg-result" class="result"></div>
            </div>
        </div>
        <div id="login-tab" class="tab-content">
            <div class="test-area">
                <label>用户名</label><input id="login-username" value="admin">
                <label>密码</label><input id="login-password" type="password" value="123456">
                <button onclick="doLogin()">登录</button>
                <div id="login-result" class="result"></div>
            </div>
        </div>
        <div id="users-tab" class="tab-content">
            <div id="users-table"></div>
            <button onclick="loadUsers()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 快递服务 -->
<div class="section">
    <div class="section-title">模块2：快递服务 <span class="badge">express.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'express-query-tab')">取件码查询</div>
            <div class="tab" onclick="switchTab(this, 'express-list-tab')">快递列表</div>
            <div class="tab" onclick="switchTab(this, 'express-data-tab')">数据表</div>
        </div>
        <div id="express-query-tab" class="tab-content active">
            <div class="test-area">
                <label>快递单号</label><input id="express-no" value="SF1234567890">
                <button onclick="queryExpress()">查询</button>
                <div id="express-query-result" class="result"></div>
            </div>
        </div>
        <div id="express-list-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="express-userid" value="1">
                <button onclick="listExpress()">获取列表</button>
                <div id="express-list-result" class="result"></div>
            </div>
        </div>
        <div id="express-data-tab" class="tab-content">
            <div id="express-table"></div>
            <button onclick="loadExpress()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 饮食订餐 -->
<div class="section">
    <div class="section-title">模块3：饮食订餐 <span class="badge">foods.json / orders.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'food-list-tab')">菜品列表</div>
            <div class="tab" onclick="switchTab(this, 'order-submit-tab')">提交订单</div>
            <div class="tab" onclick="switchTab(this, 'food-data-tab')">数据表</div>
        </div>
        <div id="food-list-tab" class="tab-content active">
            <div class="test-area">
                <button onclick="loadFoods()">获取菜品列表</button>
                <div id="food-list-result" class="result"></div>
            </div>
        </div>
        <div id="order-submit-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="order-userid" value="1">
                <button onclick="submitOrder()">提交测试订单</button>
                <div id="order-result" class="result"></div>
            </div>
        </div>
        <div id="food-data-tab" class="tab-content">
            <div id="food-table"></div>
            <button onclick="loadFoodsTable()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 热水服务 -->
<div class="section">
    <div class="section-title">模块4：热水服务 <span class="badge">hotwater.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'hw-balance-tab')">查询钱包余额</div>
            <div class="tab" onclick="switchTab(this, 'hw-use-tab')">使用热水</div>
            <div class="tab" onclick="switchTab(this, 'hw-data-tab')">数据表</div>
        </div>
        <div id="hw-balance-tab" class="tab-content active">
            <div class="test-area">
                <label>用户ID</label><input id="hw-userid" value="1">
                <button onclick="getHwBalance()">查询</button>
                <div id="hw-balance-result" class="result"></div>
            </div>
        </div>
        <div id="hw-use-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="hw-use-userid" value="1">
                <label>出水量(升)</label><input id="hw-liters" value="5">
                <label>位置</label><input id="hw-location" value="1号宿舍热水房">
                <button onclick="useHotwater()">出水</button>
                <div id="hw-use-result" class="result"></div>
            </div>
        </div>
        <div id="hw-data-tab" class="tab-content">
            <div id="hw-table"></div>
            <button onclick="loadHotwater()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 打印服务 -->
<div class="section">
    <div class="section-title">模块5：打印服务 <span class="badge">print_records.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'print-submit-tab')">提交打印</div>
            <div class="tab" onclick="switchTab(this, 'print-data-tab')">数据表</div>
        </div>
        <div id="print-submit-tab" class="tab-content active">
            <div class="test-area">
                <label>用户ID</label><input id="print-userid" value="1">
                <label>文件名</label><input id="print-filename" value="测试文档.pdf">
                <button onclick="submitPrint()">提交</button>
                <div id="print-result" class="result"></div>
            </div>
        </div>
        <div id="print-data-tab" class="tab-content">
            <div id="print-table"></div>
            <button onclick="loadPrintRecords()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 上网服务 -->
<div class="section">
    <div class="section-title">模块6：上网服务 <span class="badge">internet.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'net-status-tab')">查询状态</div>
            <div class="tab" onclick="switchTab(this, 'net-login-tab')">登录/登出</div>
            <div class="tab" onclick="switchTab(this, 'net-recharge-tab')">充值上网费</div>
            <div class="tab" onclick="switchTab(this, 'net-data-tab')">数据表</div>
        </div>
        <div id="net-status-tab" class="tab-content active">
            <div class="test-area">
                <label>用户ID</label><input id="net-userid" value="1">
                <button onclick="getNetStatus()">查询</button>
                <div id="net-status-result" class="result"></div>
            </div>
        </div>
        <div id="net-login-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="net-login-userid" value="1">
                <button onclick="netLogin()">校园网登录</button>
                <button onclick="netLogout()" style="background:#e65100;margin-top:6px;">校园网登出</button>
                <div id="net-login-result" class="result"></div>
            </div>
        </div>
        <div id="net-recharge-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="net-re-userid" value="1">
                <label>充值金额（从钱包扣款）</label><input id="net-re-amount" value="20">
                <button onclick="rechargeInternet()">充值</button>
                <div id="net-re-result" class="result"></div>
            </div>
        </div>
        <div id="net-data-tab" class="tab-content">
            <div id="net-table"></div>
            <button onclick="loadInternet()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 钱包服务 -->
<div class="section">
    <div class="section-title">模块7：钱包服务 <span class="badge">wallet.json / wallet_records.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'wallet-balance-tab')">查询余额</div>
            <div class="tab" onclick="switchTab(this, 'wallet-recharge-tab')">充值</div>
            <div class="tab" onclick="switchTab(this, 'wallet-data-tab')">数据表</div>
        </div>
        <div id="wallet-balance-tab" class="tab-content active">
            <div class="test-area">
                <label>用户ID</label><input id="wallet-userid" value="1">
                <button onclick="getWalletBalance()">查询</button>
                <div id="wallet-balance-result" class="result"></div>
            </div>
        </div>
        <div id="wallet-recharge-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="wallet-re-userid" value="1">
                <label>金额</label><input id="wallet-re-amount" value="50">
                <button onclick="rechargeWallet()">充值</button>
                <div id="wallet-re-result" class="result"></div>
            </div>
        </div>
        <div id="wallet-data-tab" class="tab-content">
            <div id="wallet-table"></div>
            <button onclick="loadWallet()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 个人信息管理 -->
<div class="section">
    <div class="section-title">模块8：个人信息管理 <span class="badge">users.json</span></div>
    <div class="section-body">
        <div class="tabs">
            <div class="tab active" onclick="switchTab(this, 'profile-get-tab')">查询用户信息</div>
            <div class="tab" onclick="switchTab(this, 'profile-update-tab')">更新用户信息</div>
            <div class="tab" onclick="switchTab(this, 'profile-data-tab')">数据表</div>
        </div>
        <div id="profile-get-tab" class="tab-content active">
            <div class="test-area">
                <label>用户ID</label><input id="profile-get-userid" value="1">
                <button onclick="getUserInfo()">查询</button>
                <div id="profile-get-result" class="result"></div>
            </div>
        </div>
        <div id="profile-update-tab" class="tab-content">
            <div class="test-area">
                <label>用户ID</label><input id="profile-up-userid" value="1">
                <label>姓名</label><input id="profile-up-name" value="马铭涛">
                <label>学号</label><input id="profile-up-sid" value="24103230119">
                <label>宿舍</label><input id="profile-up-dorm" value="5b214">
                <button onclick="updateUserInfo()">更新</button>
                <div id="profile-up-result" class="result"></div>
            </div>
        </div>
        <div id="profile-data-tab" class="tab-content">
            <div id="profile-table"></div>
            <button onclick="loadProfileTable()" style="margin-top:10px;background:#4A90D9;color:white;border:none;padding:8px 20px;border-radius:6px;cursor:pointer;">刷新数据</button>
        </div>
    </div>
</div>

<!-- 设置 -->
<div class="section">
    <div class="section-title">模块9：设置 <span class="badge">客户端本地功能</span></div>
    <div class="section-body">
        <div style="padding:20px;color:#666;line-height:1.8;">
            <p><b>设置功能为纯客户端本地功能，不涉及Web服务端API交互。</b></p>
            <p>功能说明：</p>
            <ul style="margin-left:20px;">
                <li>深色模式切换：使用Switch控件，通过SharedPreferences存储偏好（键名dark_mode）</li>
                <li>切换时调用AppCompatDelegate.setDefaultNightMode()动态切换日间/夜间主题</li>
                <li>应用启动时（MainActivity.onCreate）读取设置并应用</li>
            </ul>
            <p style="margin-top:10px;">Android关键代码：</p>
            <div style="background:#1e1e1e;color:#d4d4d4;padding:12px;border-radius:8px;font-family:monospace;font-size:12px;white-space:pre-wrap;">SharedPreferences sp = getSharedPreferences("settings_sp", MODE_PRIVATE);
boolean isDark = sp.getBoolean("dark_mode", false);
AppCompatDelegate.setDefaultNightMode(
    isDark ? AppCompatDelegate.MODE_NIGHT_YES
           : AppCompatDelegate.MODE_NIGHT_NO
);</div>
        </div>
    </div>
</div>

</div>

<script>
function switchTab(el, tabId) {
    var parent = el.parentElement;
    parent.querySelectorAll('.tab').forEach(function(t) { t.classList.remove('active'); });
    el.classList.add('active');
    var container = parent.parentElement;
    container.querySelectorAll('.tab-content').forEach(function(c) { c.classList.remove('active'); });
    document.getElementById(tabId).classList.add('active');
}

function showResult(id, data) {
    var el = document.getElementById(id);
    el.style.display = 'block';
    el.textContent = JSON.stringify(data, null, 2);
}

function jsonToTable(data, columns) {
    if (!data || data.length === 0) return '<div class="empty">暂无数据</div>';
    var html = '<table><tr>';
    columns.forEach(function(c) { html += '<th>' + c.label + '</th>'; });
    html += '</tr>';
    data.forEach(function(row) {
        html += '<tr>';
        columns.forEach(function(c) {
            var val = row[c.key];
            if (typeof val === 'object') val = JSON.stringify(val);
            html += '<td>' + (val !== undefined && val !== null ? val : '') + '</td>';
        });
        html += '</tr>';
    });
    html += '</table>';
    return html;
}

// 注册
function doRegister() {
    var body = {
        username: document.getElementById('reg-username').value,
        password: document.getElementById('reg-password').value,
        name: document.getElementById('reg-name').value,
        studentId: document.getElementById('reg-sid').value,
        dorm: document.getElementById('reg-dorm').value
    };
    fetch('/api/register', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('reg-result', d); });
}

// 登录
function doLogin() {
    var body = {
        username: document.getElementById('login-username').value,
        password: document.getElementById('login-password').value
    };
    fetch('/api/login', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('login-result', d); });
}

// 用户数据表
function loadUsers() {
    fetch('/api/admin/users').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('users-table').innerHTML = jsonToTable(d.data, [
            {key:'id', label:'ID'}, {key:'username', label:'用户名'}, {key:'password', label:'密码'},
            {key:'name', label:'姓名'}, {key:'studentId', label:'学号'}, {key:'dorm', label:'宿舍'}
        ]);
    });
}

// 快递查询
function queryExpress() {
    var no = document.getElementById('express-no').value;
    fetch('/api/express/query?trackingNo=' + no).then(function(r) { return r.json(); })
        .then(function(d) { showResult('express-query-result', d); });
}

function listExpress() {
    var uid = document.getElementById('express-userid').value;
    fetch('/api/express/list/' + uid).then(function(r) { return r.json(); })
        .then(function(d) { showResult('express-list-result', d); });
}

function loadExpress() {
    fetch('/api/admin/express').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('express-table').innerHTML = jsonToTable(d.data, [
            {key:'id', label:'ID'}, {key:'userId', label:'用户ID'}, {key:'trackingNo', label:'单号'},
            {key:'company', label:'公司'}, {key:'status', label:'状态'}, {key:'pickupCode', label:'取件码'}
        ]);
    });
}

// 菜品
function loadFoods() {
    fetch('/api/food/list').then(function(r) { return r.json(); })
        .then(function(d) { showResult('food-list-result', d); });
}

function submitOrder() {
    var uid = document.getElementById('order-userid').value;
    fetch('/api/food/list').then(function(r) { return r.json(); }).then(function(d) {
        var items = d.data.slice(0, 2).map(function(f) { return {foodId: f.id, count: 1}; });
        fetch('/api/order/submit', { method: 'POST', headers: {'Content-Type':'application/json'},
            body: JSON.stringify({userId: uid, items: items}) })
            .then(function(r) { return r.json(); })
            .then(function(d2) { showResult('order-result', d2); });
    });
}

function loadFoodsTable() {
    fetch('/api/food/list').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('food-table').innerHTML = jsonToTable(d.data, [
            {key:'id', label:'ID'}, {key:'name', label:'菜名'}, {key:'desc', label:'描述'}, {key:'price', label:'价格(元)'}
        ]);
    });
}

// 热水
function getHwBalance() {
    var uid = document.getElementById('hw-userid').value;
    fetch('/api/wallet/balance/' + uid).then(function(r) { return r.json(); })
        .then(function(d) { showResult('hw-balance-result', d); });
}

function useHotwater() {
    var body = {
        userId: document.getElementById('hw-use-userid').value,
        liters: parseInt(document.getElementById('hw-liters').value),
        location: document.getElementById('hw-location').value
    };
    fetch('/api/hotwater/use', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('hw-use-result', d); });
}

function loadHotwater() {
    fetch('/api/admin/hotwater').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('hw-table').innerHTML = jsonToTable(d.data, [
            {key:'userId', label:'用户ID'}, {key:'balance', label:'余额(元)'}
        ]);
    });
}

// 打印
function submitPrint() {
    var body = {
        userId: document.getElementById('print-userid').value,
        fileName: document.getElementById('print-filename').value,
        paper: 'A4', colorType: '黑白', side: '单面', copies: 1
    };
    fetch('/api/print/submit', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('print-result', d); });
}

function loadPrintRecords() {
    fetch('/api/admin/print_records').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('print-table').innerHTML = jsonToTable(d.data, [
            {key:'id', label:'ID'}, {key:'userId', label:'用户ID'}, {key:'fileName', label:'文件名'},
            {key:'paper', label:'纸张'}, {key:'colorType', label:'颜色'}, {key:'side', label:'单双面'},
            {key:'copies', label:'份数'}, {key:'price', label:'价格(元)'}, {key:'status', label:'状态'}, {key:'time', label:'时间'}
        ]);
    });
}

// 上网
function getNetStatus() {
    var uid = document.getElementById('net-userid').value;
    fetch('/api/internet/status/' + uid).then(function(r) { return r.json(); })
        .then(function(d) { showResult('net-status-result', d); });
}

function netLogin() {
    var uid = document.getElementById('net-login-userid').value;
    fetch('/api/internet/login', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({userId: uid}) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('net-login-result', d); });
}

function netLogout() {
    var uid = document.getElementById('net-login-userid').value;
    fetch('/api/internet/logout', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({userId: uid}) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('net-login-result', d); });
}

function rechargeInternet() {
    var body = {
        userId: document.getElementById('net-re-userid').value,
        amount: parseInt(document.getElementById('net-re-amount').value)
    };
    fetch('/api/internet/recharge', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('net-re-result', d); });
}

function loadInternet() {
    fetch('/api/admin/internet').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('net-table').innerHTML = jsonToTable(d.data, [
            {key:'userId', label:'用户ID'}, {key:'balance', label:'余额(元)'}, {key:'flowGB', label:'流量(GB)'}, {key:'isLogin', label:'是否登录'}
        ]);
    });
}

// 钱包
function getWalletBalance() {
    var uid = document.getElementById('wallet-userid').value;
    fetch('/api/wallet/balance/' + uid).then(function(r) { return r.json(); })
        .then(function(d) { showResult('wallet-balance-result', d); });
}

function rechargeWallet() {
    var body = {
        userId: document.getElementById('wallet-re-userid').value,
        amount: parseInt(document.getElementById('wallet-re-amount').value)
    };
    fetch('/api/wallet/recharge', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('wallet-re-result', d); });
}

function loadWallet() {
    fetch('/api/admin/wallet').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('wallet-table').innerHTML = jsonToTable(d.data, [
            {key:'userId', label:'用户ID'}, {key:'balance', label:'余额(元)'}
        ]);
    });
}

// 页面加载时自动加载所有数据表
window.onload = function() {
    loadUsers(); loadExpress(); loadFoodsTable(); loadHotwater(); loadPrintRecords(); loadInternet(); loadWallet(); loadProfileTable();
};

// 个人信息管理
function getUserInfo() {
    var uid = document.getElementById('profile-get-userid').value;
    fetch('/api/user/' + uid).then(function(r) { return r.json(); })
        .then(function(d) { showResult('profile-get-result', d); });
}

function updateUserInfo() {
    var body = {
        name: document.getElementById('profile-up-name').value,
        studentId: document.getElementById('profile-up-sid').value,
        dorm: document.getElementById('profile-up-dorm').value
    };
    var uid = document.getElementById('profile-up-userid').value;
    fetch('/api/user/' + uid, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
        .then(function(r) { return r.json(); })
        .then(function(d) { showResult('profile-up-result', d); });
}

function loadProfileTable() {
    fetch('/api/admin/users').then(function(r) { return r.json(); }).then(function(d) {
        document.getElementById('profile-table').innerHTML = jsonToTable(d.data, [
            {key:'id', label:'ID'}, {key:'username', label:'用户名'}, {key:'name', label:'姓名'},
            {key:'studentId', label:'学号'}, {key:'dorm', label:'宿舍'}
        ]);
    });
}
</script>
</body>
</html>
'''


# ========== 管理员数据查询API（供Web页面使用）==========

@app.route('/api/admin/users', methods=['GET'])
def admin_users():
    users = load_json('users.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": users})

@app.route('/api/admin/express', methods=['GET'])
def admin_express():
    data = load_json('express.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": data})

@app.route('/api/admin/hotwater', methods=['GET'])
def admin_hotwater():
    data = load_json('hotwater.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": data})

@app.route('/api/admin/internet', methods=['GET'])
def admin_internet():
    data = load_json('internet.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": data})

@app.route('/api/admin/wallet', methods=['GET'])
def admin_wallet():
    data = load_json('wallet.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": data})

@app.route('/api/admin/print_records', methods=['GET'])
def admin_print_records():
    data = load_json('print_records.json')
    return jsonify({"code": 200, "msg": "获取成功", "data": data})


# ========== 启动服务 ==========

if __name__ == '__main__':
    print("=" * 50)
    print("学生生活服务管理 - Web服务端")
    print("访问地址: http://0.0.0.0:5000")
    print("管理页面: http://0.0.0.0:5000/")
    print("=" * 50)
    app.run(host='0.0.0.0', port=5000, debug=True)
